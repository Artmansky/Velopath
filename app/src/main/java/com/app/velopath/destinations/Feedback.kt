package com.app.velopath.destinations

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.velopath.R
import com.app.velopath.ui.TopBar
import kotlinx.serialization.Serializable

@Serializable
object Feedback

@Composable
fun PrintFeedback(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    onFeedbackClick: (messageContent: String, name: String, onResult: (String?) -> Unit) -> Unit,
    networkAvailable: (context: Context) -> Boolean
) {
    Scaffold(
        topBar = {
            TopBar(
                title = title,
                onDrawerClick = onClick
            )
        }
    ) { contentPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            FeedbackPage(onFeedbackClick, networkAvailable)
        }
    }
}

@Composable
fun FeedbackPage(
    onClick: (messageContent: String, name: String, onResult: (String?) -> Unit) -> Unit,
    networkAvailable: (context: Context) -> Boolean
) {
    var name by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Feedback",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = context.getString(R.string.feedback_info),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(bottom = 24.dp),
            maxLines = 8
        )

        Button(
            onClick = {
                when {
                    name.isEmpty() || message.isEmpty() -> {
                        Toast.makeText(
                            context,
                            "Message and name can't be empty",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    !networkAvailable(context) -> {
                        Toast.makeText(
                            context,
                            "Network error. Check connection",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    else -> {
                        onClick(message, name) {
                            Toast.makeText(context, "Feedback sent!", Toast.LENGTH_SHORT).show()
                        }

                        name = ""
                        message = ""
                    }
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Send")
        }
    }
}