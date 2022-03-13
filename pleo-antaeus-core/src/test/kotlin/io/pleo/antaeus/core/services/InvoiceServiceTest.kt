package io.pleo.antaeus.core.services

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import org.junit.Before
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {

    @Before
    fun setUp() {
        clearAllMocks()
        unmockkAll()
    }

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { updateInvoiceStatus(1) } returns true
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun updateStatusTest() {
        every { invoiceService.updateStatus(1) } returns true
        invoiceService.updateStatus(1)
    }

    @Test
    fun fetchAllTest() {
        assertThrows<Exception> {
            invoiceService.fetchAll()
        }
    }
}
