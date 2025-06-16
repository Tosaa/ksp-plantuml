package com.firstexample.first

enum class TrafficLightColors(val hexColorValue: String) {
    RED("#FFaa00"),
    YELLOW("#aaFF00"),
    GREEN("#00aaFF");

    suspend fun switch(): Unit {

    }
}