package com.example.imilipocket.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Result
import com.example.imilipocket.data.Transaction
import com.example.imilipocket.databinding.FragmentEditTransactionBinding
import android.widget.ArrayAdapter

class EditTransactionFragment : Fragment() {
    private var _binding: FragmentEditTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditTransactionViewModel
    private val args: EditTransactionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupUI()
        observeViewModel()
    }

    private fun setupViewModel() {
        val preferenceManager = PreferenceManager(requireContext())
        viewModel = ViewModelProvider(
            this,
            EditTransactionViewModel.Factory(preferenceManager)
        )[EditTransactionViewModel::class.java]
    }

    private fun setupUI() {
        val transaction = args.transaction
        binding.apply {
            // Set up category spinner
            val categories = viewModel.getCategories()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
            
            // Set initial values
            editTextTitle.setText(transaction.title)
            editTextAmount.setText(transaction.amount.toString())
            
            // Set category selection
            val categoryPosition = categories.indexOf(transaction.category)
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition)
            }
            
            // Set transaction type
            if (transaction.type == Transaction.Type.INCOME) {
                radioIncome.isChecked = true
            } else {
                radioExpense.isChecked = true
            }

            // Set up click listeners
            buttonSave.setOnClickListener {
                updateTransaction(transaction)
            }

            buttonCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun updateTransaction(originalTransaction: Transaction) {
        binding.apply {
            val title = editTextTitle.text.toString()
            val amount = editTextAmount.text.toString().toDoubleOrNull()
            val category = spinnerCategory.selectedItem?.toString() ?: ""
            val type = if (radioIncome.isChecked) Transaction.Type.INCOME else Transaction.Type.EXPENSE

            if (title.isBlank() || amount == null || category.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            val updatedTransaction = Transaction(
                id = originalTransaction.id,
                title = title,
                amount = amount,
                category = category,
                type = type,
                date = originalTransaction.date
            )

            viewModel.updateTransaction(updatedTransaction)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    showMessage("Transaction updated successfully")
                    findNavController().navigateUp()
                },
                onFailure = { error ->
                    showMessage("Failed to update transaction: ${error.message}")
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 