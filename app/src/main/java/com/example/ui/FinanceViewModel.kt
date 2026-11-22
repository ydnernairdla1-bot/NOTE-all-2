package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ImageStorageManager
import com.example.data.NoteRepository
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FinancePeriod(val label: String) {
    THIS_MONTH("Bulan Ini"),
    LAST_7_DAYS("7 Hari"),
    ALL_TIME("Semua")
}

enum class FinanceTypeFilter(val label: String) {
    ALL("Semua"),
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran")
}

data class CategoryBreakdown(
    val category: String,
    val amount: Long,
    val fraction: Float
)

data class FinanceChartBucket(
    val label: String,
    val income: Long,
    val expense: Long
)

data class FinanceUiState(
    val period: FinancePeriod = FinancePeriod.THIS_MONTH,
    val typeFilter: FinanceTypeFilter = FinanceTypeFilter.ALL,
    val searchQuery: String = "",
    val transactions: List<TransactionEntity> = emptyList(),
    val periodTransactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val expenseBreakdown: List<CategoryBreakdown> = emptyList(),
    val statusMessage: String? = null
) {
    val netBalance: Long get() = totalIncome - totalExpense
}

fun buildFinanceChartBuckets(
    period: FinancePeriod,
    transactions: List<TransactionEntity>,
    locale: Locale,
    weekPrefix: String = "M",
    nowMillis: Long = System.currentTimeMillis()
): List<FinanceChartBucket> {
    return when (period) {
        FinancePeriod.LAST_7_DAYS -> {
            val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val labelFmt = SimpleDateFormat("EEE", locale)
            val grouped = transactions.groupBy { dayKeyFmt.format(Date(it.transactionDate)) }
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, -6)
            }
            buildList(7) {
                repeat(7) {
                    val key = dayKeyFmt.format(cal.time)
                    val txs = grouped[key].orEmpty()
                    add(
                        FinanceChartBucket(
                            label = labelFmt.format(cal.time),
                            income = txs.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount },
                            expense = txs.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
                        )
                    )
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
        FinancePeriod.THIS_MONTH -> {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val targetYear = cal.get(Calendar.YEAR)
            val targetMonth = cal.get(Calendar.MONTH)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val weekCount = ((maxDay - 1) / 7) + 1
            val incomeByWeek = LongArray(weekCount)
            val expenseByWeek = LongArray(weekCount)

            val txCal = Calendar.getInstance()
            transactions.forEach { tx ->
                txCal.timeInMillis = tx.transactionDate
                if (txCal.get(Calendar.YEAR) == targetYear && txCal.get(Calendar.MONTH) == targetMonth) {
                    val day = txCal.get(Calendar.DAY_OF_MONTH)
                    val weekIndex = ((day - 1) / 7).coerceIn(0, weekCount - 1)
                    if (tx.transactionType == TransactionType.INCOME) {
                        incomeByWeek[weekIndex] += tx.amount
                    } else {
                        expenseByWeek[weekIndex] += tx.amount
                    }
                }
            }
            (0 until weekCount).map { idx ->
                FinanceChartBucket(
                    label = "$weekPrefix${idx + 1}",
                    income = incomeByWeek[idx],
                    expense = expenseByWeek[idx]
                )
            }
        }
        FinancePeriod.ALL_TIME -> {
            val monthKeyFmt = SimpleDateFormat("yyyy-MM", Locale.US)
            val monthLabelFmt = SimpleDateFormat("MMM yy", locale)
            val grouped = transactions.groupBy { monthKeyFmt.format(Date(it.transactionDate)) }
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -5)
            }
            buildList(6) {
                repeat(6) {
                    val key = monthKeyFmt.format(cal.time)
                    val txs = grouped[key].orEmpty()
                    add(
                        FinanceChartBucket(
                            label = monthLabelFmt.format(cal.time),
                            income = txs.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount },
                            expense = txs.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
                        )
                    )
                    cal.add(Calendar.MONTH, 1)
                }
            }
        }
    }
}

class FinanceViewModel(private val repository: NoteRepository) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(FinancePeriod.THIS_MONTH)
    private val selectedTypeFilter = MutableStateFlow(FinanceTypeFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    private val statusMessage = MutableStateFlow<String?>(null)

    val imageStorageManager: ImageStorageManager
        get() = repository.imageStorageManager

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allTransactions,
        selectedPeriod,
        selectedTypeFilter,
        searchQuery,
        statusMessage
    ) { allTx, period, typeFilter, query, message ->
        val periodStartMs = calculatePeriodStartMs(period)
        val periodTx = if (periodStartMs == null) {
            allTx
        } else {
            allTx.filter { it.transactionDate >= periodStartMs }
        }

        val totalIncome = periodTx
            .filter { it.transactionType == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = periodTx
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val expenseBreakdown = if (totalExpense > 0L) {
            periodTx
                .filter { it.transactionType == TransactionType.EXPENSE }
                .groupBy { it.category }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.amount }
                    CategoryBreakdown(
                        category = cat,
                        amount = sum,
                        fraction = (sum.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f)
                    )
                }
                .sortedByDescending { it.amount }
                .take(5)
        } else {
            emptyList()
        }

        val trimmed = query.trim().lowercase()
        val filteredList = periodTx.filter { tx ->
            val matchesType = when (typeFilter) {
                FinanceTypeFilter.ALL -> true
                FinanceTypeFilter.INCOME -> tx.transactionType == TransactionType.INCOME
                FinanceTypeFilter.EXPENSE -> tx.transactionType == TransactionType.EXPENSE
            }
            val matchesQuery = trimmed.isEmpty() ||
                tx.title.lowercase().contains(trimmed) ||
                tx.category.lowercase().contains(trimmed) ||
                tx.note.lowercase().contains(trimmed)
            matchesType && matchesQuery
        }

        FinanceUiState(
            period = period,
            typeFilter = typeFilter,
            searchQuery = query,
            transactions = filteredList,
            periodTransactions = periodTx,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            expenseBreakdown = expenseBreakdown,
            statusMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FinanceUiState()
    )

    fun onPeriodSelected(period: FinancePeriod) {
        selectedPeriod.value = period
    }

    fun onTypeFilterSelected(filter: FinanceTypeFilter) {
        selectedTypeFilter.value = filter
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun saveTransaction(
        existingId: String?,
        type: TransactionType,
        amount: Long,
        title: String,
        category: String,
        note: String,
        receiptImageName: String?,
        transactionDate: Long,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveTransaction(
                existingId = existingId,
                type = type,
                amount = amount,
                title = title,
                category = category,
                note = note,
                receiptImageName = receiptImageName,
                transactionDate = transactionDate
            )
            statusMessage.value = if (existingId == null) {
                "${type.label} tersimpan"
            } else {
                "Transaksi diperbarui"
            }
            onComplete()
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            statusMessage.value = "Transaksi dihapus"
        }
    }

    suspend fun saveReceiptImage(uri: Uri): String? =
        repository.saveImageFromUri(uri)

    fun deleteUnusedReceipt(fileName: String) {
        viewModelScope.launch {
            repository.deleteImageFile(fileName)
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private fun calculatePeriodStartMs(period: FinancePeriod): Long? {
        val cal = Calendar.getInstance()
        return when (period) {
            FinancePeriod.ALL_TIME -> null
            FinancePeriod.LAST_7_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            FinancePeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
        }
    }

    companion object {
        fun factory(repository: NoteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FinanceViewModel(repository) as T
                }
            }
    }
}
