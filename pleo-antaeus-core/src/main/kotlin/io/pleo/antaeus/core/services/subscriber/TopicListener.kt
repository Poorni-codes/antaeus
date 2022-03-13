package io.pleo.antaeus.core.services.subscriber

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import javax.jms.Message
import javax.jms.MessageListener
import mu.KotlinLogging
import java.math.BigDecimal
import javax.jms.JMSException
import javax.jms.ObjectMessage

class TopicListener(private val paymentProvider: PaymentProvider, val invoiceService: InvoiceService) :
    MessageListener {

    private val logger = KotlinLogging.logger {}
    var listOfInvoiceToUpdate: MutableList<Invoice> = mutableListOf<Invoice>()

    override fun onMessage(message: Message?) {
        try {

            val mapMessage = message as ObjectMessage
            val invoiceMessage: Invoice = mapMessage.getObject() as Invoice

            //Checking the amount should not be zero
            if (invoiceMessage.amount.value != BigDecimal.ZERO) {

                listOfInvoiceToUpdate.add(invoiceMessage)

                //Billing the consumer as per Invoice
                var update: Boolean = BillingService(
                    paymentProvider = paymentProvider,
                    invoiceMessage,
                    invoiceService
                ).consumerPaymentCharge()

                //logger.info{"Update value $update"}

                //Purging successfully charged invoices
                if (update) {
                    message.jmsExpiration = 60000 //1 hour
                } else {
                    //keep the unprocessed messages in the topic for surther analysis
                }
            }
            logger.info { "Inside TopicListener: onMessage listOfInvoiceToUpdate = $listOfInvoiceToUpdate" }
        } catch (e: NumberFormatException) {
            logger.error { "Inside TopicListener : Exception $e" }
            e.printStackTrace()
        } catch (e: JMSException) {
            logger.error { "Inside TopicListener : Exception $e" }
            e.printStackTrace()
        }
    }

}
