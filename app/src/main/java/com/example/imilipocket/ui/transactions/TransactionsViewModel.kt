package com.example.imilipocket.ui.transactions

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val preferenceManager: PreferenceManager,
    private val context: Context
) : ViewModel() {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            try {
                preferenceManager.getTransactions().collectLatest { transactions ->
                    _transactions.value = transactions.sortedByDescending { it.date }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _transactions.value = emptyList()
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                preferenceManager.addTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                preferenceManager.updateTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                preferenceManager.deleteTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCategories(): List<String> {
        return try {
            context.resources.getStringArray(R.array.transaction_categories).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    class Factory(
        private val preferenceManager: PreferenceManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TransactionsViewModel(preferenceManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 