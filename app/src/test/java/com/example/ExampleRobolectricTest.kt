package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.PayeeEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAppName() {
        val appName = context.getString(R.string.app_name)
        assertEquals("ExpenseSync", appName)
    }

    @Test
    fun testPayeeInsertionAndFetch() = runBlocking {
        val payee = PayeeEntity(
            id = "payee-1",
            name = "Radhakrishna Textiles",
            categoryTag = "Fabric",
            hasGstBill = true,
            gstNumber = "32AAAAA0000A1Z5",
            isDefault = true
        )
        db.payeeDao().insertPayee(payee)

        val retrieved = db.payeeDao().getPayeeById("payee-1")
        assertNotNull(retrieved)
        assertEquals("Radhakrishna Textiles", retrieved?.name)
        assertEquals("Fabric", retrieved?.categoryTag)
        assertTrue(retrieved?.hasGstBill == true)
        assertEquals("32AAAAA0000A1Z5", retrieved?.gstNumber)
        assertTrue(retrieved?.isDefault == true)
    }

    @Test
    fun testTransactionWithPayeeAndGst() = runBlocking {
        val cat = CategoryEntity(id = "cat-1", name = "Materials", iconName = "inventory", colorHex = "#386B3B")
        val src = PaymentSourceEntity(id = "src-1", type = "SB", bankName = "HDFC Bank", isDefault = true)
        val payee = PayeeEntity(
            id = "payee-2",
            name = "Accessories Tailors Mart Aluva",
            categoryTag = "Accessories",
            hasGstBill = true,
            gstNumber = "32ABCDE1234F1Z9"
        )
        val txn = TransactionEntity(
            id = "txn-1",
            amount = 1450.0,
            transactionDate = System.currentTimeMillis(),
            categoryId = cat.id,
            sourceId = src.id,
            payeeId = payee.id,
            merchantName = payee.name,
            hasGstBill = true,
            gstNumber = payee.gstNumber
        )

        db.categoryDao().insertCategory(cat)
        db.paymentSourceDao().insertPaymentSource(src)
        db.payeeDao().insertPayee(payee)
        db.transactionDao().insertTransaction(txn)

        val txnsWithDetails = db.transactionDao().getAllTransactionsWithDetails().first()
        assertEquals(1, txnsWithDetails.size)
        val firstItem = txnsWithDetails[0]
        assertEquals(1450.0, firstItem.transaction.amount, 0.01)
        assertEquals("Accessories Tailors Mart Aluva", firstItem.payee?.name)
        assertEquals("Accessories", firstItem.payee?.categoryTag)
        assertTrue(firstItem.transaction.hasGstBill)
        assertEquals("32ABCDE1234F1Z9", firstItem.transaction.gstNumber)
    }

    @Test
    fun testCashProjectionCalculation() {
        val salaryPayee = PayeeEntity(
            id = "salary-1",
            name = "Staff Salary - Master Tailor",
            categoryTag = "Salary",
            defaultAmount = 28000.0,
            isRecurring = true,
            dueDayOfMonth = 1,
            recurringType = "SALARY"
        )
        val rentPayee = PayeeEntity(
            id = "rent-1",
            name = "Shop Owner Rent",
            categoryTag = "Rent",
            defaultAmount = 18000.0,
            isRecurring = true,
            dueDayOfMonth = 5,
            recurringType = "RENT"
        )

        val projection = com.example.data.model.CashProjectionEngine.calculateProjection(
            payees = listOf(salaryPayee, rentPayee),
            categories = emptyList(),
            transactions = emptyList(),
            lookaheadDays = 31,
            notificationsEnabled = true,
            alertThresholdDays = 3
        )

        assertEquals(46000.0, projection.totalRequiredAmount, 0.01)
        assertEquals(28000.0, projection.salaryTotal, 0.01)
        assertEquals(18000.0, projection.rentTotal, 0.01)
        assertEquals(2, projection.allDebits.size)
    }
}
