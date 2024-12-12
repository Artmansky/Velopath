package com.app.velopath.database

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.firestore.ktx.persistentCacheSettings
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil

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
        onResult: () -> Unit,
        onFail: () -> Unit
    ) {
        user?.uid.let { id ->
            val message = hashMapOf(
                "UID" to id,
                "Name" to name,
                "Message" to messageContent,
            )

            db.collection("Feedback")
                .add(message)
                .addOnSuccessListener {
                    onResult()
                }
                .addOnFailureListener {
                    onFail()
                }
        }
    }

    fun addNewRoute(
        routeName: String,
        overviewPolyline: String,
        navigationLink: String,
        distance: Double,
        duration: Int,
        onResult: () -> Unit,
        onFail: () -> Unit
    ) {
        val points = PolyUtil.decode(overviewPolyline)

        user?.uid.let { id ->
            val newRoute = hashMapOf(
                "authorID" to id,
                "title" to routeName,
                "navLink" to navigationLink,
                "startLang" to points[0].latitude,
                "startLong" to points[0].longitude,
                "engLang" to points[points.size - 1].latitude,
                "longLang" to points[points.size - 1].longitude,
                "distance" to distance,
                "duration" to duration,
                "polyline" to overviewPolyline
            )

            db.collection("Routes")
                .add(newRoute)
                .addOnSuccessListener {
                    onResult()
                }
                .addOnFailureListener {
                    onFail()
                }
        }
    }

    fun deleteRoute(id: String, onResult: () -> Unit, onFail: () -> Unit) {
        db.collection("Routes").document(id).delete()
            .addOnSuccessListener {
                onResult()
            }
            .addOnFailureListener {
                onFail()
            }
    }
}