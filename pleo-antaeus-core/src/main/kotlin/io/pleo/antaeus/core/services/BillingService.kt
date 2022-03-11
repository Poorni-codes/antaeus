package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

class BillingService(
    private val paymentProvider: PaymentProvider,
    pendingStatusConsumed: Invoice,
    private val invoiceService: InvoiceService
) {

    private val logger = KotlinLogging.logger {}
    private val consumerUpdateToDB: Invoice = pendingStatusConsumed

    fun consumerPaymentCharge() {
        logger.info { "Inside BillingService : consumerPaymentCharge :  $consumerUpdateToDB" }
        when (paymentProvider.charge(consumerUpdateToDB)) {
            true -> invoiceService.updateStatusToPaid(consumerUpdateToDB.id)
        }

        logger.info { "$invoiceService.updateStatusToPaid( consumerUpdateToDB.id)" }

    }
}
