package com.example.leafy

import android.app.Application
import com.example.leafy.data.LeafyDatabase

class LeafyApp : Application() {

    val database: LeafyDatabase by lazy { LeafyDatabase.getDatabase(this) }
}