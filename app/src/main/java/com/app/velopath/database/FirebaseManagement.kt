package com.app.velopath.database

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.firestore.ktx.persistentCacheSettings
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import kotlin.math.abs

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
                "title" to routeName,
                "author" to id,
                "navigationLink" to navigationLink,
                "startLang" to points[0].latitude,
                "startLong" to points[0].longitude,
                "endLang" to points[points.size - 1].latitude,
                "endLong" to points[points.size - 1].longitude,
                "distance" to distance,
                "duration" to duration,
                "overviewPolyline" to overviewPolyline
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

    fun fetchNearbyRoutes(
        latLng: LatLng,
        onResult: (List<RouteItem>) -> Unit,
        onFail: () -> Unit
    ) {
        db.collection("Routes").get()
            .addOnSuccessListener { documents ->
                val emptyRouteList: MutableList<RouteItem> = mutableListOf()
                if (documents.isEmpty) {
                    onResult(emptyList())
                } else {
                    for (document in documents) {
                        try {
                            val startLang = document.getDouble("startLang")
                            val startLong = document.getDouble("startLong")
                            if (abs(startLang!! - latLng.latitude) <= 0.01 && abs(startLong!! - latLng.longitude) <= 0.01) {
                                val newItem = RouteItem(
                                    document.id,
                                    document.getString("title")!!,
                                    document.getString("author")!!,
                                    document.getString("navigationLink")!!,
                                    startLang,
                                    startLong,
                                    document.getDouble("endLang")!!,
                                    document.getDouble("endLong")!!,
                                    document.getDouble("distance")!!,
                                    document.getLong("duration")!!.toInt(),
                                    document.getString("overviewPolyline")!!
                                )
                                emptyRouteList.add(newItem)
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                    onResult(emptyRouteList)
                }
            }
            .addOnFailureListener {
                onFail()
            }
    }

    fun fetchAuthorRoutes(
        author: String?,
        onResult: (List<RouteItem>) -> Unit,
        onFail: () -> Unit
    ) {
        db.collection("Routes").whereEqualTo("author", author).get()
            .addOnSuccessListener { documents ->
                val emptyRouteList: MutableList<RouteItem> = mutableListOf()
                if (documents.isEmpty) {
                    onResult(emptyList())
                } else {
                    for (document in documents) {
                        try {
                            val newItem = RouteItem(
                                document.id,
                                document.getString("title")!!,
                                document.getString("author")!!,
                                document.getString("navigationLink")!!,
                                document.getDouble("startLang")!!,
                                document.getDouble("startLong")!!,
                                document.getDouble("endLang")!!,
                                document.getDouble("endLong")!!,
                                document.getDouble("distance")!!,
                                document.getLong("duration")!!.toInt(),
                                document.getString("overviewPolyline")!!
                            )
                            emptyRouteList.add(newItem)
                        } catch (e: Exception) {
                            continue
                        }
                    }
                    onResult(emptyRouteList)
                }
            }
            .addOnFailureListener {
                onFail()
            }
    }
}