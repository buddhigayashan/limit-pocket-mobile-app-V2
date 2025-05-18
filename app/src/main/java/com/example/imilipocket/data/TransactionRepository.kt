package com.example.imilipocket.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions().map { entities ->
        entities.map { it.toTransaction() }
    }

    suspend fun getTransactionById(id: String): Transaction? {
        return transactionDao.getTransactionById(id)?.toTransaction()
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    private fun TransactionEntity.toTransaction(): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            category = category,
            type = type,
            date = date
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            title = title,
            amount = amount,
            category = category,
            type = type,
            date = date
        )
    }
} 