package com.app.velopath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.app.velopath.ui.theme.VelopathTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    private val currentUser: StateFlow<FirebaseUser?> = _currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            VelopathTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    auth.addAuthStateListener { firebaseAuth ->
                        _currentUser.value = firebaseAuth.currentUser
                    }
                    val user by currentUser.collectAsState()
                    val clientID = getString(R.string.default_web_client_id)

                    MainNavigation(user, auth, clientID)
                }
            }
        }
    }
}