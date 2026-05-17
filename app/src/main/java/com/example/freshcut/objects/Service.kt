package com.example.freshcut.objects


// will act as diffrentiating between service provided
// for example hair / hair+beard / beard maybe something else depends on the admin

data class Service(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val duration: Int = 0 // Minutes
)
