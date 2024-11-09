package com.app.velopath

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.firestore.ktx.persistentCacheSettings
import com.google.firebase.ktx.Firebase

class FirebaseManagement(private var user: FirebaseUser?) {
    private val db = Firebase.firestore

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {})
            setLocalCacheSettings(persistentCacheSettings {})
        }
        db.firestoreSettings = settings
    }

    fun updateCurrentUser(newUser: FirebaseUser?) {
        user = newUser
    }

    fun addFeedbackMessage(
        messageContent: String,
        name: String,
        onResult: (String?) -> Unit
    ) {
        user?.uid.let { id ->
            val message = hashMapOf(
                "UID" to id,
                "Name" to name,
                "Message" to messageContent,
            )

            db.collection("Feedback")
                .add(message)
                .addOnFailureListener { e ->
                    onResult(e.message)
                }
        }
    }
}