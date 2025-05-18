package com.example.imilipocket.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTransactionType(type: Transaction.Type): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(value: String): Transaction.Type {
        return Transaction.Type.valueOf(value)
    }
} 