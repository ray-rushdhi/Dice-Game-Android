package com.example.mobilecw

import android.app.Application

class SaveData : Application() { // A subclass of the application class which can be used to store data
    // Reference - https://stackoverflow.com/questions/4208886/using-the-android-application-class-to-persist-data
    var humanWins = 0
    var compWins = 0
}