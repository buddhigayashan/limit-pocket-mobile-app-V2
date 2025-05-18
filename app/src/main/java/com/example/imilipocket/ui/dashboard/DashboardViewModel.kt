package com.example.imilipocket.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _monthlyBudget = MutableLiveData<Double>()
    val monthlyBudget: LiveData<Double> = _monthlyBudget

    private val _remainingBudget = MutableLiveData<Double>()
    val remainingBudget: LiveData<Double> = _remainingBudget

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData<Double>()
    val totalExpense: LiveData<Double> = _totalExpense

    private val _totalBalance = MutableLiveData<Double>(0.0)
    val totalBalance: LiveData<Double> = _totalBalance

    private val _monthlyExpenses = MutableLiveData<Double>(0.0)
    val monthlyExpenses: LiveData<Double> = _monthlyExpenses

    private val _categorySpending = MutableLiveData<Map<String, Double>>(emptyMap())
    val categorySpending: LiveData<Map<String, Double>> = _categorySpending

    init {
        loadDashboardData()
    }

    fun refreshData() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                preferenceManager.getTransactions().collectLatest { transactions ->
                    _transactions.value = transactions
                    updateTotals(transactions)
                    calculateCategorySpending(transactions)
                }
                _monthlyBudget.value = preferenceManager.getMonthlyBudget()
                updateRemainingBudget()
            } catch (e: Exception) {
                e.printStackTrace()
                _transactions.value = emptyList()
                _totalBalance.value = 0.0
                _totalIncome.value = 0.0
                _totalExpense.value = 0.0
                _monthlyBudget.value = 0.0
                _monthlyExpenses.value = 0.0
                _remainingBudget.value = 0.0
                _categorySpending.value = emptyMap()
            }
        }
    }

    private fun updateTotals(transactions: List<Transaction>) {
        val income = transactions
            .filter { it.type == Transaction.Type.INCOME }
            .sumOf { it.amount }
        val expense = transactions
            .filter { it.type == Transaction.Type.EXPENSE }
            .sumOf { it.amount }

        _totalIncome.value = income
        _totalExpense.value = expense
        _totalBalance.value = income - expense
        updateRemainingBudget()
    }

    private fun updateRemainingBudget() {
        val budget = _monthlyBudget.value ?: 0.0
        val expenses = _totalExpense.value ?: 0.0
        _remainingBudget.value = budget - expenses
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

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                preferenceManager.deleteTransaction(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateCategorySpending(transactions: List<Transaction>) {
        try {
            val categoryMap = transactions
                .filter { it.type == Transaction.Type.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            _categorySpending.value = categoryMap
        } catch (e: Exception) {
            e.printStackTrace()
            _categorySpending.value = emptyMap()
        }
    }

    class Factory(private val preferenceManager: PreferenceManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(preferenceManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 
