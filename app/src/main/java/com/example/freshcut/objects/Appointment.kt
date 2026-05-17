package com.example.freshcut.objects

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Appointment (
    var id: String = "",
    var userId: String = "",
    var userName: String ="",
    var serviceId: String = "",
    var serviceName: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    var duration: Int? = 0,
)