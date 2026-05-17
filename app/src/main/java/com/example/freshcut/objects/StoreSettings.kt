package com.example.freshcut.objects

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class StoreSettings (
    var openingTime: String,
    var closingTime: String,
    var blockedDates: List<String>, // format: dd/MM/yy
    var workingDays: List<Boolean> // each index represent a different day of the week


){
    @get:Exclude
    val parsedOpeningTime: LocalTime
        get() = LocalTime.parse(openingTime)

    @get:Exclude
    val parsedClosingTime: LocalTime
        get() = LocalTime.parse(closingTime)
    @get:Exclude
    val parsedBlockedDates: List<LocalDate>
        get() = blockedDates.map { LocalDate.parse(it) }
}