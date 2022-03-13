package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvalidCurrencyTypeException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class BillingService(
    private val paymentProvider: PaymentProvider,
    invoiceMessage: Invoice,
    private val invoiceService: InvoiceService
) {

    private val logger = KotlinLogging.logger {}
    private val invoiceMessageToUpdate: Invoice = invoiceMessage

    fun consumerPaymentCharge(): Boolean {
        logger.info { "Inside BillingService : consumerPaymentCharge :  $invoiceMessageToUpdate" }

        var currencyMismatch: Boolean = hasCurrency()
        logger.info { "Value currencyMismatch : $currencyMismatch" }
        var statusUpdate: Boolean = false

        if (!currencyMismatch && invoiceMessageToUpdate.status != InvoiceStatus.PAID) {
            statusUpdate = when (paymentProvider.charge(invoiceMessageToUpdate)) {
                invoiceService.updateStatus(invoiceMessageToUpdate.id) -> true
                !(invoiceService.updateStatus(invoiceMessageToUpdate.id)) -> throw InvoiceNotFoundException(
                    invoiceMessageToUpdate.id
                )
                else -> false
            }
        }
        logger.info { "Inside BillingService : consumerPaymentCharge $statusUpdate" }
        return statusUpdate
    }

    //Currency mismatch check - Future use cases
    private fun hasCurrency(): Boolean = when (invoiceMessageToUpdate.amount.currency) {
        Currency.DKK -> false
        Currency.GBP -> false
        Currency.SEK -> false
        Currency.EUR -> false
        Currency.USD -> false
        else -> throw InvalidCurrencyTypeException(
            invoiceMessageToUpdate.customerId,
            invoiceMessageToUpdate.amount.currency
        )
    }
}
