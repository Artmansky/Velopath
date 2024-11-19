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
import com.app.velopath.ui.PreferencesManager
import com.app.velopath.ui.theme.ThemeMode
import com.app.velopath.ui.theme.VelopathTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel: MainViewModel
    private val database = FirebaseManagement(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(applicationContext)
        viewModel = MainViewModel(preferencesManager)

        setContent {
            val themeMode by viewModel.darkTheme.collectAsState()

            VelopathTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val user by viewModel.currentUser.collectAsState()
                    database.updateCurrentUser(user)
                    val clientID = getString(R.string.default_web_client_id)

                    MainNavigation(
                        user = user,
                        viewModel = viewModel,
                        clientID = clientID,
                        database = database,
                        themeMode = themeMode,
                        onDarkThemeChange = { isDark ->
                            val newMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
                            viewModel.saveTheme(newMode)
                        }
                    )
                }
            }
        }
    }
}