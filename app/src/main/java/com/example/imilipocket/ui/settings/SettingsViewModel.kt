package com.example.imilipocket.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class SettingsViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
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

    fun setSelectedCurrency(currency: String) {
        val currencyCode = currency.substring(0, 3)
        preferenceManager.setSelectedCurrency(currencyCode)
    }

    private fun loadMonthlyTransactions() {
        viewModelScope.launch {
            try {
                preferenceManager.getTransactions().collectLatest { transactions: List<Transaction> ->
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

    class Factory(private val preferenceManager: PreferenceManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(preferenceManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 