package com.example.imilipocket.data

import android.content.Context
import android.content.SharedPreferences
import com.example.imilipocket.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val context: Context = context
    private val database = AppDatabase.getDatabase(context)
    private val repository = TransactionRepository(database.transactionDao())

    companion object {
        private const val PREFS_NAME = "ImiliPocketPrefs"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_SELECTED_CURRENCY = "selected_currency"
        private const val KEY_MONTH_CYCLE_START_DAY = "month_cycle_start_day"
        private const val DEFAULT_CURRENCY = "USD"
        private const val DEFAULT_MONTH_CYCLE_START_DAY = 1
    }

    fun saveMonthlyBudget(budget: Double) {
        try {
            sharedPreferences.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMonthlyBudget(): Double {
        return try {
            sharedPreferences.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    fun getMonthCycleStartDay(): Int {
        return try {
            sharedPreferences.getInt(KEY_MONTH_CYCLE_START_DAY, DEFAULT_MONTH_CYCLE_START_DAY)
        } catch (e: Exception) {
            e.printStackTrace()
            DEFAULT_MONTH_CYCLE_START_DAY
        }
    }

    fun setMonthCycleStartDay(day: Int) {
        try {
            if (day in 1..31) {
                sharedPreferences.edit().putInt(KEY_MONTH_CYCLE_START_DAY, day).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTransactions(): Flow<List<Transaction>> = repository.allTransactions

    suspend fun addTransaction(transaction: Transaction) {
        try {
            repository.insertTransaction(transaction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        try {
            repository.updateTransaction(transaction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        try {
            repository.deleteTransaction(transaction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSelectedCurrency(): String {
        return try {
            sharedPreferences.getString(KEY_SELECTED_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
        } catch (e: Exception) {
            e.printStackTrace()
            DEFAULT_CURRENCY
        }
    }

    fun setSelectedCurrency(currency: String) {
        try {
            sharedPreferences.edit().putString(KEY_SELECTED_CURRENCY, currency).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCategories(): List<String> {
        return try {
            context.resources.getStringArray(R.array.transaction_categories).toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
} 