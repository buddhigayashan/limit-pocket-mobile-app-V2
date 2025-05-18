package com.example.imilipocket.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import com.example.imilipocket.databinding.DialogAddTransactionBinding
import com.example.imilipocket.databinding.FragmentDashboardBinding
import com.example.imilipocket.ui.transaction.TransactionAdapter
import com.example.imilipocket.util.CurrencyFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupUI()
        setupClickListeners()
        observeViewModel()
        setupPieChart()
    }

    private fun setupViewModel() {
        val preferenceManager = PreferenceManager(requireContext())
        viewModel = ViewModelProvider(
            this,
            DashboardViewModel.Factory(preferenceManager)
        )[DashboardViewModel::class.java]
    }

    private fun setupUI() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onEditClick = { transaction ->
                // TODO: Handle edit transaction
            },
            onDeleteClick = { transaction ->
                showDeleteConfirmationDialog(transaction)
            }
        )
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setHoleColor(android.R.color.transparent)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(true)
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(android.R.color.black)
            setHoleRadius(40f)
            setTransparentCircleRadius(45f)
        }
    }

    private fun updatePieChart() {
        try {
            val transactions = viewModel.transactions.value ?: emptyList()
            val monthlyBudget = viewModel.monthlyBudget.value ?: 0.0
            
            // Group expenses by category
            val categoryExpenses = transactions
                .filter { it.type == Transaction.Type.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> 
                    transactions.sumOf { it.amount }
                }
                .toList()
                .sortedByDescending { it.second }

            val entries = mutableListOf<PieEntry>()
            
            // Add category expenses
            categoryExpenses.forEach { (category, amount) ->
                entries.add(PieEntry(amount.toFloat(), category))
            }

            // Add remaining budget if there are expenses
            if (categoryExpenses.isNotEmpty()) {
                val totalExpenses = categoryExpenses.sumOf { it.second }
                val remaining = monthlyBudget - totalExpenses
                if (remaining > 0) {
                    entries.add(PieEntry(remaining.toFloat(), "Remaining"))
                }
            }

            val dataSet = PieDataSet(entries, "Expenses by Category").apply {
                colors = listOf(
                    resources.getColor(R.color.category_food, null),
                    resources.getColor(R.color.category_bills, null),
                    resources.getColor(R.color.category_transport, null),
                    resources.getColor(R.color.category_shopping, null),
                    resources.getColor(R.color.category_other, null),
                    resources.getColor(R.color.chart_color_1, null),
                    resources.getColor(R.color.chart_color_2, null),
                    resources.getColor(R.color.chart_color_3, null),
                    resources.getColor(R.color.chart_color_4, null),
                    resources.getColor(R.color.chart_color_5, null)
                )
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return CurrencyFormatter.formatAmount(requireContext(), value.toDouble())
                    }
                }
                valueTextSize = 12f
                valueTextColor = android.R.color.black
                setDrawValues(true)
            }

            binding.pieChart.data = PieData(dataSet)
            binding.pieChart.invalidate()
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error updating pie chart: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteTransaction(transaction)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog() {
        val dialogBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        
        // Setup category dropdown
        val categories = PreferenceManager(requireContext()).getCategories()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        dialogBinding.spinnerCategory.setAdapter(adapter)

        // Set default transaction type to expense
        dialogBinding.rgType.check(R.id.rb_expense)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setTitle("Add Transaction")
            .setPositiveButton("Save") { dialog, _ ->
                try {
                    val title = dialogBinding.etTitle.text.toString()
                    val amount = dialogBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
                    val category = dialogBinding.spinnerCategory.text.toString()
                    val type = if (dialogBinding.rbIncome.isChecked) {
                        Transaction.Type.INCOME
                    } else {
                        Transaction.Type.EXPENSE
                    }

                    if (title.isNotEmpty() && amount > 0 && category.isNotEmpty()) {
                        val transaction = Transaction(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            amount = amount,
                            category = category,
                            type = type,
                            date = System.currentTimeMillis()
                        )
                        viewModel.addTransaction(transaction)
                    }
                } catch (e: Exception) {
                    Log.e("DashboardFragment", "Error adding transaction: ${e.message}")
                    e.printStackTrace()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.remainingBudget.observe(viewLifecycleOwner) { budget ->
            binding.tvRemainingBudget.text = CurrencyFormatter.formatAmount(requireContext(), budget ?: 0.0)
            updatePieChart()
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvTotalIncome.text = CurrencyFormatter.formatAmount(requireContext(), income ?: 0.0)
            updatePieChart()
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.tvTotalExpense.text = CurrencyFormatter.formatAmount(requireContext(), expense ?: 0.0)
            updatePieChart()
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)
            updatePieChart()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 