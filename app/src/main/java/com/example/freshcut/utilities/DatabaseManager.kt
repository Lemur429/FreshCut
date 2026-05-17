package com.example.freshcut.utilities

import android.util.Log
import android.widget.Toast
import com.example.freshcut.objects.Appointment
import com.example.freshcut.objects.Service
import com.example.freshcut.objects.StoreSettings
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.UUID

object DatabaseManager {
    private val firestore = Firebase.firestore

    private val serviceCollection = firestore.collection("Service")
    private val appointmentCollection = firestore.collection("Appointment")
    val settingsCollection = firestore.collection("StoreSettings")

    fun createService(service: Service,context: android.content.Context)
    {
        serviceCollection
            .add(service)
            .addOnSuccessListener { docRef-> Toast.makeText(
                context,
                "success "+docRef.toString(),
                Toast.LENGTH_SHORT,
            ).show()}
            .addOnFailureListener { e -> Toast.makeText(
                context,
                "failure"+e.toString(),
                Toast.LENGTH_LONG,
            ).show()
                Log.e("what",e.toString())
            }
    }
    fun isUserManager(userId: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit)
    {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document!=null &&  document.exists())
                {
                    val isAdmin = document.getString("role").equals("admin")
                    onSuccess(isAdmin)
                } else  {
                    onFailure(Exception("User profile not found in database."))
                }
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    fun saveUserOnDB(user: HashMap<String, String?>)
    {
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser

        currentFirebaseUser?.let { firebaseUser ->
            firestore.collection("Users").document(firebaseUser.uid)
                .set(user) // 'user' here is your data class/map with fullName, etc.
                .addOnSuccessListener {
                    // Success
                }
        }
    }

    fun getServicesList(ready: (List<Service>?) -> Unit)
    {
        serviceCollection.get().addOnSuccessListener { snapshot ->
            val objects=snapshot.toObjects(Service::class.java)
            ready(objects)
        }.addOnFailureListener { e -> Log.d("ERROR","ERRORRR")
        throw e}

    }

    fun updateStoreSettings(settings: StoreSettings)
    {
        settingsCollection
            .document("main")
            .set(settings) // This replaces the entire document with your object
            .addOnSuccessListener {
                cleanupInvalidAppointments(settings,{},{})
                Log.d("StoreSettings", "Successfully saved all settings!")
            }
            .addOnFailureListener { e ->
                Log.e("StoreSettings", "Failed to save: ${e.message}")
            }
    }
    fun getStoreSettings(ready: (StoreSettings) -> Unit)
    {


        settingsCollection
            .get()
            .addOnSuccessListener { snapshot ->

                val document = snapshot.documents[0]

                val openingTime = document.get("openingTime") as String
                val closingTime = document.get("closingTime") as String
                val workingDays= document.get("workingDays") as List<Boolean>
                val blockedDates = document.get("blockedDates") as List<String>
                val settings = StoreSettings(openingTime,closingTime,blockedDates,workingDays);

                ready(settings)
            }.addOnFailureListener { e ->
                Log.d("ERROR","errorrr"+ Firebase.auth.uid.toString())
                e.printStackTrace()}
    }

    fun getBookedTimesFromFirebase(selectedDate: LocalDate, onSuccess: (Set<LocalTime>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // 1. Figure out the start and end of the selected day
        val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

        // Convert them to Firebase Timestamps
        val startTimestamp = Timestamp(Date.from(startOfDay))
        val endTimestamp = Timestamp(Date.from(endOfDay))

        // 2. Query the appointments collection
        appointmentCollection // Make sure this matches your actual collection name
            .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
            .whereLessThan("timestamp", endTimestamp)
            .get()
            .addOnSuccessListener { documents ->
                val bookedTimes = mutableSetOf<LocalTime>()

                for (document in documents) {
                    val appointment = document.toObject(Appointment::class.java)

                    // 3. Extract the time from the Firebase Timestamp
                    appointment.timestamp?.let { fbTimestamp ->
                        val localTime = fbTimestamp.toDate()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()

                        bookedTimes.add(localTime)
                    }
                }

                Log.d("SUCCESS", "appointments")

                // 4. Send the found times back to the UI
                onSuccess(bookedTimes)
            }
            .addOnFailureListener { e ->
                Log.d("ERROR","failed appointments")
                e.printStackTrace()
                onSuccess(emptySet())
            }
    }

    fun createAppointment(
        date: LocalDate,
        time: String,
        serviceId: String,
        serviceName: String,
        duration: Int?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )
    {

        // 3. Parse the Time String and convert to Firebase Timestamp safely
        val timeParts = time.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val exactDateTime = date.atTime(hour, minute)
        val zonedDateTime = exactDateTime.atZone(ZoneId.systemDefault())
        val firebaseTimestamp = Timestamp(Date.from(zonedDateTime.toInstant()))

        // 4. Generate a unique ID for the document
        val appointmentId = UUID.randomUUID().toString()

        // 5. Create the Data Object (Using the Appointment class we defined earlier)
        val newAppointment = Appointment(
            id = appointmentId,
            duration = duration,
            serviceId = serviceId,
            serviceName = serviceName,
            timestamp = firebaseTimestamp,
            userId = AuthManager.getCurrentUser()?.uid ?: "ERROR",
            userName = AuthManager.getCurrentUser()?.displayName ?: "Unknown Client"
        )

        // 6. Save to Firebase Firestore
        appointmentCollection
            .document(appointmentId)
            .set(newAppointment)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getFutureAppointments(
        isManager: Boolean,
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var query = appointmentCollection.whereGreaterThan("timestamp", Timestamp.now())

        if (!isManager) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            query = query.whereEqualTo("userId", currentUserId)
        }
        query.get()
            .addOnSuccessListener { snapshot ->
                val appointmentsFromDb = snapshot.toObjects(Appointment::class.java)
                onSuccess(appointmentsFromDb)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getTodayAppointments(
        isManager: Boolean,
        onSuccess: (List<Appointment>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val startOfToday: Date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfToday: Date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        var query = appointmentCollection
            .whereGreaterThanOrEqualTo("timestamp", startOfToday)
            .whereLessThanOrEqualTo("timestamp", endOfToday)

        if (!isManager) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            query = query.whereEqualTo("userId", currentUserId)
        }
        query.get()
            .addOnSuccessListener { snapshot ->
                val appointments = snapshot.toObjects(Appointment::class.java)
                onSuccess(appointments)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
    fun deleteAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        // Make sure "appointments" matches your actual Firebase collection name!
        appointmentCollection
            .document(appointmentId)
            .delete()
            .addOnSuccessListener {
                onSuccess() // Tell the UI it worked!
            }
            .addOnFailureListener { exception ->
                onFailure(exception) // Tell the UI it failed!
            }
    }
    fun cleanupInvalidAppointments(
        newSettings: StoreSettings,
        onSuccess: (Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        appointmentCollection
            .whereGreaterThan("timestamp", Timestamp.now())
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                val openTime = newSettings.parsedOpeningTime
                val closeTime =newSettings.parsedClosingTime
                // 1. FILTER: Create a list containing ONLY the bad appointments
                val invalidDocuments = documents.filter { document ->
                    val appt = document.toObject(Appointment::class.java)
                    val apptInstant = appt.timestamp?.toDate()?.toInstant() ?: return@filter false
                    val apptDate = apptInstant.atZone(ZoneId.systemDefault())

                    val localDate = apptDate.toLocalDate()
                    val localTime = apptDate.toLocalTime()
                    val dayOfWeekIndex = apptDate.dayOfWeek.value % 7

                    // The 3 Rules
                    val breaksDayRule = dayOfWeekIndex < newSettings.workingDays.size && !newSettings.workingDays[dayOfWeekIndex]
                    val breaksDateRule = newSettings.blockedDates.contains(localDate.toString())
                    val breaksTimeRule = localTime.isBefore(openTime) || localTime.isAfter(closeTime)

                    // If ANY of these are true, the filter keeps it in the "invalid" list
                    breaksDayRule || breaksDateRule || breaksTimeRule
                }

// 2. ACTION: Tell the batch to delete everything in that list
                invalidDocuments.forEach { document ->
                    batch.delete(document.reference)
                }

// 3. EXECUTE
                val deletedCount = invalidDocuments.size
                if (deletedCount > 0) {
                    batch.commit()
                        .addOnSuccessListener { onSuccess(deletedCount) }
                        .addOnFailureListener { onFailure(it) }
                } else {
                    onSuccess(0)
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}