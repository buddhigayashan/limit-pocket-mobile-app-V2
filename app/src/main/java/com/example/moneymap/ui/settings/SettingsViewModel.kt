package com.example.moneymap.ui.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferenceManager: PreferenceManager = PreferenceManager(application)
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        "app_preferences",
        Application.MODE_PRIVATE
    )

    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> = _currency

    private val _monthlyTransactions = MutableLiveData<List<Transaction>>()
    val monthlyTransactions: LiveData<List<Transaction>> = _monthlyTransactions

    init {
        loadCurrency()
        loadMonthlyTransactions()
    }

    private fun loadCurrency() {
        _currency.value = preferenceManager.getSelectedCurrency()
    }

    fun updateCurrency(newCurrency: String) {
        preferenceManager.setSelectedCurrency(newCurrency)
        _currency.value = newCurrency
    }

    private fun loadMonthlyTransactions() {
        viewModelScope.launch {
            try {
                preferenceManager.getTransactions().collect { transactions: List<Transaction> ->
                    val currentCalendar = Calendar.getInstance()
                    val currentMonth = currentCalendar.get(Calendar.MONTH)
                    val currentYear = currentCalendar.get(Calendar.YEAR)

                    val monthlyTransactions = transactions.filter { transaction ->
                        val transactionCalendar = Calendar.getInstance()
                        transactionCalendar.timeInMillis = transaction.date
                        transactionCalendar.get(Calendar.MONTH) == currentMonth && 
                        transactionCalendar.get(Calendar.YEAR) == currentYear
                    }
                    _monthlyTransactions.value = monthlyTransactions
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _monthlyTransactions.value = emptyList()
            }
        }
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }
} 