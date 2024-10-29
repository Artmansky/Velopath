package com.artmansky.velopath.login

interface AuthResponse {
    data object Success : AuthResponse
    data class Error(val message: String) : AuthResponse
}