package com.app.velopath.database

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
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
        addNewUserDocumentIfNotExists()
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
                    incrementTotalAdd()
                    incrementCurrentAdd()
                    incrementTotalRoutes()
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
                decrementCurrentAdd()
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
                        document.data.let { documentData ->
                            if (abs((documentData["startLang"] as Double) - latLng.latitude) <= 0.03 && abs(
                                    (documentData["startLong"] as Double) - latLng.longitude
                                ) <= 0.03
                            ) {
                                val newItem = RouteItem(
                                    document.id,
                                    documentData["title"] as String,
                                    documentData["author"] as String,
                                    documentData["navigationLink"] as String,
                                    documentData["startLang"] as Double,
                                    documentData["startLong"] as Double,
                                    documentData["endLang"] as Double,
                                    documentData["endLong"] as Double,
                                    documentData["distance"] as Double,
                                    (documentData["duration"] as Long).toInt(),
                                    documentData["overviewPolyline"] as String
                                )
                                emptyRouteList.add(newItem)
                            }
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
                        document.data.let { documentData ->
                            val newItem = RouteItem(
                                document.id,
                                documentData["title"] as String,
                                documentData["author"] as String,
                                documentData["navigationLink"] as String,
                                documentData["startLang"] as Double,
                                documentData["startLong"] as Double,
                                documentData["endLang"] as Double,
                                documentData["endLong"] as Double,
                                documentData["distance"] as Double,
                                (documentData["duration"] as Long).toInt(),
                                documentData["overviewPolyline"] as String
                            )
                            emptyRouteList.add(newItem)
                        }
                    }
                    onResult(emptyRouteList)
                }
            }
            .addOnFailureListener {
                onFail()
            }
    }

    fun addLiked(routeID: String, onResult: () -> Unit, onFail: () -> Unit) {
        user?.uid?.let { id ->
            if (id.isNotEmpty()) {
                val documentRef = db.collection("Saved").document(id)
                documentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val likedRoutes =
                                document.get("likedRoutes") as? List<String> ?: emptyList()
                            if (routeID in likedRoutes) {
                                onFail()
                            } else {
                                documentRef.update(
                                    "likedRoutes",
                                    FieldValue.arrayUnion(routeID)
                                ).addOnSuccessListener {
                                    incrementTotalRoutes()
                                    incrementTotalLiked()
                                    onResult()
                                }.addOnFailureListener {
                                    onFail()
                                }
                            }
                        } else {
                            val initialData = mapOf(
                                "likedRoutes" to listOf(routeID)
                            )
                            documentRef.set(initialData)
                                .addOnSuccessListener {
                                    incrementTotalLiked()
                                    onResult()
                                }.addOnFailureListener {
                                    onFail()
                                }
                        }
                    }
                    .addOnFailureListener {
                        onFail()
                    }
            }
        }
    }

    fun removeLiked(routeID: String, onResult: () -> Unit, onFail: () -> Unit) {
        user?.uid?.let { id ->
            if (id.isNotEmpty()) {
                val documentRef = db.collection("Saved").document(id)
                documentRef.update("likedRoutes", FieldValue.arrayRemove(routeID))
                    .addOnSuccessListener {
                        decrementTotalLiked()
                        decrementTotalRoutes()
                        onResult()
                    }
                    .addOnFailureListener { exception ->
                        if (exception is FirebaseFirestoreException) {
                            onResult()
                        } else {
                            onFail()
                        }
                    }
            }
        }
    }

    fun fetchLikedRoutes(onResult: (List<RouteItem>) -> Unit, onFail: () -> Unit) {
        user?.uid?.let { id ->
            if (id.isNotEmpty()) {
                val documentRef = db.collection("Saved").document(id)
                documentRef.get()
                    .addOnSuccessListener { document ->
                        val likedRoutes =
                            document.get("likedRoutes") as? List<String> ?: emptyList()
                        if (likedRoutes.isEmpty()) {
                            onResult(emptyList())
                        } else {
                            fetchRouteDetails(likedRoutes, onResult = { list ->
                                onResult(list)
                            })
                        }
                    }
                    .addOnFailureListener {
                        onFail()
                    }
            }
        }
    }

    fun fetchUserStats(
        author: String?,
        onResult: (Stats) -> Unit,
        onFail: () -> Unit
    ) {
        if (author != null) {
            db.collection("stats").document(author).get()
                .addOnSuccessListener { document ->
                    document.data.let { documentData ->
                        if (documentData != null) {
                            val newItem = Stats(
                                (documentData["totalAdd"] as Long).toInt(),
                                (documentData["currentAdd"] as Long).toInt(),
                                (documentData["totalLiked"] as Long).toInt(),
                                (documentData["totalRoutes"] as Long).toInt()
                            )
                            onResult(newItem)
                        }
                    }
                }
                .addOnFailureListener {
                    onFail()
                }
        }
    }

    private fun fetchRouteDetails(
        likedRoutes: List<String>,
        onResult: (List<RouteItem>) -> Unit
    ) {
        val routeItems = mutableListOf<RouteItem>()
        var completedCount = 0

        likedRoutes.forEach { routeID ->
            db.collection("Routes").document(routeID).get()
                .addOnSuccessListener { document ->
                    document.data?.let { documentData ->
                        val newItem = RouteItem(
                            document.id,
                            documentData["title"] as String,
                            documentData["author"] as String,
                            documentData["navigationLink"] as String,
                            documentData["startLang"] as Double,
                            documentData["startLong"] as Double,
                            documentData["endLang"] as Double,
                            documentData["endLong"] as Double,
                            documentData["distance"] as Double,
                            (documentData["duration"] as Long).toInt(),
                            documentData["overviewPolyline"] as String
                        )
                        routeItems.add(newItem)
                    }
                    completedCount++

                    if (completedCount == likedRoutes.size) {
                        onResult(routeItems)
                    }
                }
                .addOnFailureListener {
                    completedCount++

                    if (completedCount == likedRoutes.size) {
                        onResult(routeItems)
                    }
                }
        }
    }

    private fun addNewUserDocumentIfNotExists() {
        user?.uid.let { id ->
            if (id != null) {
                db.collection("stats").document(id).get()
                    .addOnSuccessListener { document ->
                        if (!document.exists()) {
                            val userData = mapOf(
                                "totalAdd" to 0,
                                "currentAdd" to 0,
                                "totalLiked" to 0,
                                "totalRoutes" to 0
                            )

                            db.collection("stats").document(id).set(userData)
                        }
                    }
            }
        }
    }

    private fun incrementTotalAdd() {
        user?.uid?.let { id ->
            db.collection("stats").document(id).update("totalAdd", FieldValue.increment(1))
        }
    }

    private fun incrementCurrentAdd() {
        user?.uid?.let { id ->
            db.collection("stats").document(id).update("currentAdd", FieldValue.increment(1))
        }
    }

    private fun decrementCurrentAdd() {
        user?.uid?.let { id ->
            db.collection("stats").document(id).update("currentAdd", FieldValue.increment(-1))
        }
    }

    private fun incrementTotalLiked() {
        user?.uid?.let { id ->
            db.collection("stats").document(id).update("totalLiked", FieldValue.increment(1))
        }
    }

    private fun decrementTotalLiked() {
        user?.uid?.let { id ->
            db.collection("stats").document(id).update("totalLiked", FieldValue.increment(-1))
        }
    }

    private fun incrementTotalRoutes() {
        user?.uid?.let { id ->
            db.collection("stats").document(id).update("totalRoutes", FieldValue.increment(1))
        }
    }

    private fun decrementTotalRoutes() {
        user?.uid?.let { id ->
            db.collection("stats").document(id).update("totalRoutes", FieldValue.increment(-1))
        }
    }
}