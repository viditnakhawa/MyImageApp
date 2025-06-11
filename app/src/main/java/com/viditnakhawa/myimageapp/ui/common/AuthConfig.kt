package com.viditnakhawa.myimageapp.ui.common

import android.net.Uri
import net.openid.appauth.AuthorizationServiceConfiguration

object AuthConfig {
    // Hugging Face Client ID.
    const val clientId = "88a0ac25-fcf4-467b-b8cd-ebcc2aec9bd0"

    // IMPORTANT: This MUST match the scheme in your AndroidManifest.xml
    const val redirectUri = "com.viditnakhawa.myimageapp.oauth://oauthredirect" // <-- Change this if your package name is different

    // OAuth 2.0 Endpoints (Authorization + Token Exchange)
    private const val authEndpoint = "https://huggingface.co/oauth/authorize"
    private const val tokenEndpoint = "https://huggingface.co/oauth/token"

    // OAuth service configuration (AppAuth library requires this)
    val authServiceConfig = AuthorizationServiceConfiguration(
        Uri.parse(authEndpoint), // Authorization endpoint
        Uri.parse(tokenEndpoint) // Token exchange endpoint
    )
}