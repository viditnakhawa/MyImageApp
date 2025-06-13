package com.viditnakhawa.myimageapp.ui.modelmanager

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viditnakhawa.myimageapp.GemmaIntegration
import com.viditnakhawa.myimageapp.data.*
import com.viditnakhawa.myimageapp.ui.common.AuthConfig
import com.viditnakhawa.myimageapp.ui.common.LlmChatModelHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "ModelManagerViewModel"

// --- Data classes and Enums specific to this ViewModel ---
data class ModelInitializationStatus(
    val status: ModelInitializationStatusType, var error: String = ""
)
enum class ModelInitializationStatusType {
    NOT_INITIALIZED, INITIALIZING, INITIALIZED, ERROR
}
enum class TokenStatus {
    NOT_STORED, EXPIRED, NOT_EXPIRED,
}
enum class TokenRequestResultType {
    FAILED, SUCCEEDED, USER_CANCELLED
}
data class TokenStatusAndData(
    val status: TokenStatus,
    val data: AccessTokenData?,
)
data class TokenRequestResult(
    val status: TokenRequestResultType, val errorMessage: String? = null
)
data class ModelManagerUiState(
    val tasks: List<Task>,
    val modelDownloadStatus: Map<String, ModelDownloadStatus>,
    val modelInitializationStatus: Map<String, ModelInitializationStatus>,
    val selectedModel: Model = GEMMA_E2B_MODEL,
)

class ModelManagerViewModel(
    private val downloadRepository: DownloadRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(createEmptyUiState())
    val uiState = _uiState.asStateFlow()

    val authService = AuthorizationService(context)
    var curAccessToken: String = ""

    init {
        loadInitialState()
    }

    private fun createEmptyUiState(): ModelManagerUiState {
        return ModelManagerUiState(
            tasks = emptyList(),
            modelDownloadStatus = emptyMap(),
            modelInitializationStatus = emptyMap()
        )
    }

    private fun getModelDownloadStatus(model: Model, context: Context): ModelDownloadStatus {
        val file = File(context.getExternalFilesDir(null), "${model.normalizedName}/${model.version}/${model.downloadFileName}")
        return if (file.exists() && file.length() >= model.sizeInBytes) {
            ModelDownloadStatus(status = ModelDownloadStatusType.SUCCEEDED, totalBytes = file.length(), receivedBytes = file.length())
        } else {
            ModelDownloadStatus(status = ModelDownloadStatusType.NOT_DOWNLOADED)
        }
    }

    private fun loadInitialState() {
        val model = GEMMA_E2B_MODEL
        val downloadStatus = getModelDownloadStatus(model, context)
        val initialState = ModelManagerUiState(
            tasks = listOf(ASK_IMAGE_TASK),
            modelDownloadStatus = mapOf(model.name to downloadStatus),
            modelInitializationStatus = mapOf(model.name to ModelInitializationStatus(ModelInitializationStatusType.NOT_INITIALIZED))
        )
        _uiState.update { initialState }
    }

    private fun updateModelInitializationStatus(
        model: Model, status: ModelInitializationStatusType, error: String = ""
    ) {
        val newStatuses = _uiState.value.modelInitializationStatus.toMutableMap()
        newStatuses[model.name] = ModelInitializationStatus(status = status, error = error)
        _uiState.update { it.copy(modelInitializationStatus = newStatuses) }
    }

    override fun onCleared() {
        super.onCleared()
        authService.dispose()
    }

    fun downloadModel(task: Task, model: Model) {
        setDownloadStatus(
            curModel = model, status = ModelDownloadStatus(status = ModelDownloadStatusType.IN_PROGRESS)
        )
        downloadRepository.downloadModel(
            model, onStatusUpdated = this::setDownloadStatus
        )
    }

    fun cancelDownload(model: Model) {
        // This function will tell the repository to stop the download for the given model.
        downloadRepository.cancelDownload(model)
    }

    fun deleteDownload(model: Model) {
        // This function will tell the repository to delete the download for the given model.
        downloadRepository.deleteDownload(model)
    }

    fun initializeModel(context: Context, model: Model) {
        Log.d("ModelManagerViewModel", "Initializing model: ${model.name}")
        updateModelInitializationStatus(model, ModelInitializationStatusType.INITIALIZING)

        viewModelScope.launch(Dispatchers.IO) {
            val error = GemmaIntegration.initialize(context)
            if (error.isEmpty()) {
                updateModelInitializationStatus(model, ModelInitializationStatusType.INITIALIZED)
            } else {
                updateModelInitializationStatus(model, ModelInitializationStatusType.ERROR, error)
            }
        }
    }

    fun setDownloadStatus(curModel: Model, status: ModelDownloadStatus) {
        val newStatuses = _uiState.value.modelDownloadStatus.toMutableMap()
        newStatuses[curModel.name] = status
        _uiState.update { it.copy(modelDownloadStatus = newStatuses) }
    }

    fun getModelUrlResponse(model: Model, accessToken: String? = null): Int {
        try {
            val url = URL(model.url)
            val connection = url.openConnection() as HttpURLConnection
            if (accessToken != null) {
                connection.setRequestProperty(
                    "Authorization", "Bearer $accessToken"
                )
            }
            connection.connect()
            return connection.responseCode
        } catch (e: Exception) {
            Log.e(TAG, "Network request failed", e)
            return -1
        }
    }

    fun getTokenStatusAndData(): TokenStatusAndData {
        val tokenData = dataStoreRepository.readAccessTokenData()
        if (tokenData != null) {
            val isExpired = System.currentTimeMillis() >= tokenData.expiresAtMs
            return if (isExpired) {
                TokenStatusAndData(TokenStatus.EXPIRED, tokenData)
            } else {
                curAccessToken = tokenData.accessToken
                TokenStatusAndData(TokenStatus.NOT_EXPIRED, tokenData)
            }
        }
        return TokenStatusAndData(TokenStatus.NOT_STORED, null)
    }

    fun getAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            AuthConfig.authServiceConfig,
            AuthConfig.clientId,
            ResponseTypeValues.CODE,
            Uri.parse(AuthConfig.redirectUri)
        ).setScope("read-repos").build()
    }

    fun handleAuthResult(result: ActivityResult, onTokenRequested: (TokenRequestResult) -> Unit) {
        val data = result.data
        if (data == null) {
            onTokenRequested(TokenRequestResult(TokenRequestResultType.FAILED, "Auth result data is null"))
            return
        }

        val response = AuthorizationResponse.fromIntent(data)
        val exception = AuthorizationException.fromIntent(data)

        when {
            response?.authorizationCode != null -> {
                authService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, tokenEx ->
                    if (tokenResponse?.accessToken != null) {
                        dataStoreRepository.saveAccessTokenData(
                            tokenResponse.accessToken!!,
                            tokenResponse.refreshToken ?: "",
                            tokenResponse.accessTokenExpirationTime!!
                        )
                        curAccessToken = tokenResponse.accessToken!!
                        onTokenRequested(TokenRequestResult(TokenRequestResultType.SUCCEEDED))
                    } else {
                        onTokenRequested(TokenRequestResult(TokenRequestResultType.FAILED, tokenEx?.message ?: "Token exchange failed"))
                    }
                }
            }
            exception != null -> {
                onTokenRequested(TokenRequestResult(TokenRequestResultType.USER_CANCELLED, "User cancelled flow"))
            }
            else -> {
                onTokenRequested(TokenRequestResult(TokenRequestResultType.USER_CANCELLED, "User cancelled"))
            }
        }
    }
}