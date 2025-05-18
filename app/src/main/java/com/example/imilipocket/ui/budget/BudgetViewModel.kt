package com.example.imilipocket.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudgetViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _budget = MutableLiveData<Double>(0.0)
    val budget: LiveData<Double> = _budget

    private val _monthlyExpenses = MutableLiveData<Double>(0.0)
    val monthlyExpenses: LiveData<Double> = _monthlyExpenses

    init {
        loadBudget()
        loadMonthlyExpenses()
    }

    private fun loadBudget() {
        try {
            _budget.value = preferenceManager.getMonthlyBudget()
        } catch (e: Exception) {
            e.printStackTrace()
            _budget.value = 0.0
        }
    }

    private fun loadMonthlyExpenses() {
        viewModelScope.launch {
            try {
                preferenceManager.getTransactions().collectLatest { transactions ->
                    val expenses = transactions
                        .filter { it.type == Transaction.Type.EXPENSE }
                        .sumOf { it.amount }
                    _monthlyExpenses.value = expenses
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _monthlyExpenses.value = 0.0
            }
        }
    }

    fun updateBudget(newBudget: Double) {
        try {
            preferenceManager.saveMonthlyBudget(newBudget)
            _budget.value = newBudget
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class Factory(private val preferenceManager: PreferenceManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BudgetViewModel(preferenceManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 