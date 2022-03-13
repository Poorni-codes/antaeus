package io.pleo.antaeus.core.services

import io.mockk.*
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal

class BillingServiceTest {

    @Before
    fun setUp() {
        clearAllMocks()
        unmockkAll()
    }

    private var depositAmountToTest = BigDecimal("34.12345678")
    private val moneyTest = Money(depositAmountToTest, Currency.GBP)
    private val invoiceTest = Invoice(1, 1, moneyTest, InvoiceStatus.PENDING)

    private var depositAmountToPaidTest = BigDecimal("34.12345678")
    private val moneyPaidTest = Money(depositAmountToTest, Currency.GBP)
    private val invoicePaidTest = Invoice(1, 1, moneyTest, InvoiceStatus.PAID)


    private val paymentProviderMock = mockk<PaymentProvider>()
    private val invoiceServiceMock = mockk<InvoiceService>()
    private val billingService = BillingService(paymentProviderMock, invoiceTest, invoiceServiceMock)

    @Test
    fun consumerPaymentChargeTest() {

        every { paymentProviderMock.charge(invoiceTest) } returns true
        every { invoiceServiceMock.updateStatus(1) } returns true

        billingService.consumerPaymentCharge()

        verify { paymentProviderMock.charge(invoiceTest) }
        verify { invoiceServiceMock.updateStatus(1) }
    }

    @Test
    fun consumerPaymentChargeFailTest() {

        every { paymentProviderMock.charge(invoiceTest) } returns false
        every { invoiceServiceMock.updateStatus(0) } returns false

        verify(exactly = 0) { paymentProviderMock.charge(invoicePaidTest) }
    }

}