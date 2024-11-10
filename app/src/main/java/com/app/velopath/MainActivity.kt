package com.app.velopath

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.app.velopath.ui.theme.VelopathTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var auth: FirebaseAuth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    private val currentUser: StateFlow<FirebaseUser?> = _currentUser
    private val database = FirebaseManagement(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        preferencesManager = PreferencesManager(applicationContext)

        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
        
        setContent {
            val darkTheme by preferencesManager.darkThemeFlow.collectAsState(initial = false)
            VelopathTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val user by currentUser.collectAsState()
                    database.updateCurrentUser(user)
                    val clientID = getString(R.string.default_web_client_id)

                    MainNavigation(user, auth, clientID, database, darkTheme) { isDark ->
                        lifecycleScope.launch {
                            preferencesManager.saveDarkThemePreference(isDark)
                        }
                    }
                }
            }
        }
    }
}