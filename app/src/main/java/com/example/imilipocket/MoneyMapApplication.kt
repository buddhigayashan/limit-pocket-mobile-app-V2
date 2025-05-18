package com.example.imilipocket

import android.app.Application
import com.example.imilipocket.data.AppDatabase

class MoneyMapApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
} 