package com.example.leafy

import android.app.Application
import com.example.leafy.data.LeafyDatabase // Pastikan ini sesuai nama database Anda

class LeafyApp : Application() {
    // Inisialisasi database secara 'lazy' (hanya dibuat saat dibutuhkan)
    val database: LeafyDatabase by lazy { LeafyDatabase.getDatabase(this) }
}