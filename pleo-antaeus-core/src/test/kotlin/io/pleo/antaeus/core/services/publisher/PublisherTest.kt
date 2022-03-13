package io.pleo.antaeus.core.services.publisher

import io.mockk.*
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.Before
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PublisherTest {

    @Before
    fun setUp() {
        clearAllMocks()
        unmockkAll()
    }

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoices() } returns emptyList()
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun fetchAllTest() {
        var depositAmountToTest = BigDecimal("34.12345678")
        val moneyTest = Money(depositAmountToTest, Currency.GBP)
        val invoiceTest = Invoice(1, 1, moneyTest, InvoiceStatus.PENDING)

        every { invoiceService.fetchAll() } returns listOf(invoiceTest, invoiceTest)

        invoiceService.fetchAll()
        verify(exactly = 1) { invoiceService.fetchAll() }

    }
}
