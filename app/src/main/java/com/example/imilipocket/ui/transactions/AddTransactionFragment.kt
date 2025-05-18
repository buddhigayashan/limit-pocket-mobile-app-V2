package com.example.imilipocket.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import com.example.imilipocket.databinding.FragmentAddTransactionBinding
import android.widget.ArrayAdapter

class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddTransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
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
            AddTransactionViewModel.Factory(preferenceManager, requireContext())
        )[AddTransactionViewModel::class.java]
    }

    private fun setupUI() {
        binding.apply {
            // Set up category spinner
            val categories = viewModel.getCategories()
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories.toTypedArray()
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            // Set up click listeners
            buttonSave.setOnClickListener {
                saveTransaction()
            }

            buttonCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun saveTransaction() {
        binding.apply {
            val title = editTextTitle.text.toString()
            val amount = editTextAmount.text.toString().toDoubleOrNull()
            val category = spinnerCategory.selectedItem?.toString() ?: ""
            val type = if (radioIncome.isChecked) Transaction.Type.INCOME else Transaction.Type.EXPENSE

            if (title.isBlank() || amount == null || category.isBlank()) {
                showMessage("Please fill all fields")
                return
            }

            val transaction = Transaction(
                id = System.currentTimeMillis().toString(), // Convert to String
                title = title,
                amount = amount,
                category = category,
                type = type,
                date = System.currentTimeMillis()
            )

            viewModel.addTransaction(transaction)
        }
    }

    private fun observeViewModel() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    showMessage("Transaction saved successfully")
                    findNavController().navigateUp()
                },
                onFailure = { error ->
                    showMessage("Failed to save transaction: ${error.message}")
                }
            )
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 