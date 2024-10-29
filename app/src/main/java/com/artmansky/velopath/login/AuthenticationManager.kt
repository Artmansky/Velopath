package com.artmansky.velopath.login

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthenticationManager {
    private val auth = Firebase.auth

    fun createAccountWithEmail(email: String, password: String): Flow<AuthResponse> = callbackFlow {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success)
                } else {
                    trySend(
                        AuthResponse.Error(
                            message = task.exception?.message ?: "Unknown Error"
                        )
                    )
                }
            }
    }

    fun loginWithEmail(email: String, password: String): Flow<AuthResponse> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success)
                } else {
                    trySend(
                        AuthResponse.Error(
                            message = task.exception?.message ?: "Unknown Error"
                        )
                    )
                }
            }
    }
}