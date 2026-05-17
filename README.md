# FreshCut ✂️

FreshCut is a native Android app I built to handle scheduling and appointment management for barbershops (and other service businesses). 

It splits the experience into two parts: one for customers to book haircuts, and one for managers to set their working hours and handle the schedule.

## What it does

* **Role-Based Accounts:** Customers and Managers get completely different views and permissions.
* **Smart Calendar:** The booking calendar automatically blocks out past dates, fully booked days, and days the manager marks as closed.
* **Store Settings:** Managers can change opening hours, pick working days, and block out holidays.
* **Auto-Cleanup:** If a manager updates the store hours or closes a specific day, the app automatically runs a Firebase Batch write to find and delete any appointments that are now invalid.

## Tech Stack

* Kotlin
* Firebase Authentication (Email/Password)
* Cloud Firestore (NoSQL Database)
