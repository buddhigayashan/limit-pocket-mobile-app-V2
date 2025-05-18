package com.example.imilipocket.ui.budget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.data.Transaction
import com.example.imilipocket.databinding.FragmentBudgetBinding
import com.example.imilipocket.util.NotificationHelper
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewModel: BudgetViewModel
    private lateinit var notificationHelper: NotificationHelper
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        preferenceManager = PreferenceManager(requireContext())
        viewModel = ViewModelProvider(this, BudgetViewModel.Factory(preferenceManager))
            .get(BudgetViewModel::class.java)
        notificationHelper = NotificationHelper(requireContext())
    }

    private fun setupUI() {
        setupBudgetInput()
        setupChart()
    }

    private fun setupBudgetInput() {
        binding.etMonthlyBudget.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val budgetText = binding.etMonthlyBudget.text.toString()
                if (budgetText.isNotEmpty()) {
                    try {
                        val budget = budgetText.toDouble()
                        viewModel.updateBudget(budget)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -45f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveBudget.setOnClickListener {
            saveBudget()
        }
        
        binding.btnSaveMonthCycle.setOnClickListener {
            saveMonthCycle()
        }
    }

    private fun saveMonthCycle() {
        try {
            val day = binding.etMonthCycleStartDay.text.toString().toIntOrNull()
            if (day == null || day !in 1..31) {
                Toast.makeText(requireContext(), "Please enter a valid day (1-31)", Toast.LENGTH_SHORT).show()
                return
            }
            preferenceManager.setMonthCycleStartDay(day)
            updateBudgetProgress()
            updateLineChart()
            Toast.makeText(requireContext(), "Month cycle updated", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving month cycle", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            binding.etMonthlyBudget.setText(budget.toString())
            updateLineChart()
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            updateLineChart()
        }
    }

    private fun saveBudget() {
        try {
            val budget = binding.etMonthlyBudget.text.toString().toDouble()
            if (budget < 0) {
                Toast.makeText(requireContext(), "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                return
            }
            viewModel.updateBudget(budget)
            showBudgetNotification()
            Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving budget", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBudgetNotification() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val monthlyBudget = preferenceManager.getMonthlyBudget()
                notificationHelper.showBudgetNotification(monthlyBudget)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateBudgetProgress() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthlyExpenses = viewModel.monthlyExpenses.value ?: 0.0
            val progress = if (monthlyBudget > 0) {
                (monthlyExpenses / monthlyBudget * 100).toInt()
            } else {
                0
            }
            binding.progressBudget.progress = progress
            binding.tvBudgetStatus.text = "$progress%"
        } catch (e: Exception) {
            e.printStackTrace()
            binding.progressBudget.progress = 0
            binding.tvBudgetStatus.text = "0%"
        }
    }

    private fun updateLineChart() {
        try {
            val monthlyBudget = preferenceManager.getMonthlyBudget()
            val monthCycleStartDay = preferenceManager.getMonthCycleStartDay()
            
            // Get the current date and adjust for month cycle
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            if (currentDay < monthCycleStartDay) {
                calendar.add(Calendar.MONTH, -1)
            }
            
            // Set the start date to the cycle start day
            calendar.set(Calendar.DAY_OF_MONTH, monthCycleStartDay)
            val startDate = calendar.timeInMillis
            
            // Get the end date (current date)
            val endDate = System.currentTimeMillis()
            
            // Create daily entries
            val entries = mutableListOf<Entry>()
            var runningTotal = monthlyBudget
            
            // Add initial point
            entries.add(Entry(startDate.toFloat(), monthlyBudget.toFloat()))
            
            // Add current point
            entries.add(Entry(endDate.toFloat(), runningTotal.toFloat()))

            val dataSet = LineDataSet(entries, "Budget").apply {
                color = ContextCompat.getColor(requireContext(), R.color.green_500)
                lineWidth = 2f
                setDrawCircles(true)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
            }

            binding.lineChart.data = LineData(dataSet)
            binding.lineChart.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.lineChart.setNoDataText("Error loading budget data")
            binding.lineChart.invalidate()
        }
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val currency = preferenceManager.getSelectedCurrency()
            val locale = when (currency) {
                "USD" -> Locale.US
                "EUR" -> Locale.GERMANY
                "GBP" -> Locale.UK
                "JPY" -> Locale.JAPAN
                "INR" -> Locale("en", "IN")
                "AUD" -> Locale("en", "AU")
                "CAD" -> Locale("en", "CA")
                "LKR" -> Locale("si", "LK")
                "CNY" -> Locale("zh", "CN")
                "SGD" -> Locale("en", "SG")
                "MYR" -> Locale("ms", "MY")
                "THB" -> Locale("th", "TH")
                "IDR" -> Locale("id", "ID")
                "PHP" -> Locale("en", "PH")
                "VND" -> Locale("vi", "VN")
                "KRW" -> Locale("ko", "KR")
                "AED" -> Locale("ar", "AE")
                "SAR" -> Locale("ar", "SA")
                "QAR" -> Locale("ar", "QA")
                else -> Locale.US
            }
            val format = NumberFormat.getCurrencyInstance(locale)
            format.format(amount)
        } catch (e: Exception) {
            e.printStackTrace()
            "$0.00"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 