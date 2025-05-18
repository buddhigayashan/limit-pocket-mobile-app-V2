package com.example.imilipocket.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import kotlinx.coroutines.launch

class EditTransactionViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                preferenceManager.updateTransaction(transaction)
                _updateResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }

    fun getCategories(): List<String> {
        return preferenceManager.getCategories()
    }

    class Factory(private val preferenceManager: PreferenceManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditTransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EditTransactionViewModel(preferenceManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 