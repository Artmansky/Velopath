package com.artmansky.velopath.login

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)