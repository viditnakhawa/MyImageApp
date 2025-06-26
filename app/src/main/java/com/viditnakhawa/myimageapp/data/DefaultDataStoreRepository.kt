package com.viditnakhawa.myimageapp.data


import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

data class AccessTokenData(
        val accessToken: String,
        val refreshToken: String,
        val expiresAtMs: Long
    )

interface DataStoreRepository {
        fun saveAccessTokenData(accessToken: String, refreshToken: String, expiresAt: Long)
        fun clearAccessTokenData()
        fun readAccessTokenData(): AccessTokenData?
    }

class DefaultDataStoreRepository(
        private val dataStore: DataStore<Preferences>
    ) : DataStoreRepository {

        private object PreferencesKeys {
            val ENCRYPTED_ACCESS_TOKEN = stringPreferencesKey("encrypted_access_token")
            val ACCESS_TOKEN_IV = stringPreferencesKey("access_token_iv")
            val ENCRYPTED_REFRESH_TOKEN = stringPreferencesKey("encrypted_refresh_token")
            val REFRESH_TOKEN_IV = stringPreferencesKey("refresh_token_iv")
            val ACCESS_TOKEN_EXPIRES_AT = longPreferencesKey("access_token_expires_at")
        }

        private val keystoreAlias: String = "com.viditnakhawa.myimageapp.access_token_key"
        private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        override fun saveAccessTokenData(accessToken: String, refreshToken: String, expiresAt: Long) {
            runBlocking {
                val (encryptedAccessToken, accessTokenIv) = encrypt(accessToken)
                val (encryptedRefreshToken, refreshTokenIv) = encrypt(refreshToken)
                dataStore.edit { preferences ->
                    preferences[PreferencesKeys.ENCRYPTED_ACCESS_TOKEN] = encryptedAccessToken
                    preferences[PreferencesKeys.ACCESS_TOKEN_IV] = accessTokenIv
                    preferences[PreferencesKeys.ENCRYPTED_REFRESH_TOKEN] = encryptedRefreshToken
                    preferences[PreferencesKeys.REFRESH_TOKEN_IV] = refreshTokenIv
                    preferences[PreferencesKeys.ACCESS_TOKEN_EXPIRES_AT] = expiresAt
                }
            }
        }

        override fun clearAccessTokenData() {
            return runBlocking {
                dataStore.edit { preferences ->
                    preferences.remove(PreferencesKeys.ENCRYPTED_ACCESS_TOKEN)
                    preferences.remove(PreferencesKeys.ACCESS_TOKEN_IV)
                    preferences.remove(PreferencesKeys.ENCRYPTED_REFRESH_TOKEN)
                    preferences.remove(PreferencesKeys.REFRESH_TOKEN_IV)
                    preferences.remove(PreferencesKeys.ACCESS_TOKEN_EXPIRES_AT)
                }
            }
        }

        override fun readAccessTokenData(): AccessTokenData? {
            return runBlocking {
                val preferences = dataStore.data.first()
                val encryptedAccessToken = preferences[PreferencesKeys.ENCRYPTED_ACCESS_TOKEN]
                val encryptedRefreshToken = preferences[PreferencesKeys.ENCRYPTED_REFRESH_TOKEN]
                val accessTokenIv = preferences[PreferencesKeys.ACCESS_TOKEN_IV]
                val refreshTokenIv = preferences[PreferencesKeys.REFRESH_TOKEN_IV]
                val expiresAt = preferences[PreferencesKeys.ACCESS_TOKEN_EXPIRES_AT]

                var decryptedAccessToken: String? = null
                var decryptedRefreshToken: String? = null
                if (encryptedAccessToken != null && accessTokenIv != null) {
                    decryptedAccessToken = decrypt(encryptedAccessToken, accessTokenIv)
                }
                if (encryptedRefreshToken != null && refreshTokenIv != null) {
                    decryptedRefreshToken = decrypt(encryptedRefreshToken, refreshTokenIv)
                }
                if (decryptedAccessToken != null && decryptedRefreshToken != null && expiresAt != null) {
                    AccessTokenData(decryptedAccessToken, decryptedRefreshToken, expiresAt)
                } else {
                    null
                }
            }
        }

        private fun getOrCreateSecretKey(): SecretKey {
            return (keyStore.getKey(keystoreAlias, null) as? SecretKey) ?: run {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keystoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        }

        private fun encrypt(plainText: String): Pair<String, String> {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT) to Base64.encodeToString(
                iv,
                Base64.DEFAULT
            )
        }

        private fun decrypt(encryptedText: String, ivText: String): String? {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val ivBytes = Base64.decode(ivText, Base64.DEFAULT)
            val spec = javax.crypto.spec.GCMParameterSpec(128, ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            return try {
                String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
        }
    }