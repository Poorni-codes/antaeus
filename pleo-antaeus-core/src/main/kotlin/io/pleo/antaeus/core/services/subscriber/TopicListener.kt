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

class TopicListener(s: String, val paymentProvider: PaymentProvider, val invoiceService: InvoiceService) :
    MessageListener {

    private val logger = KotlinLogging.logger {}
    var listOfToUpdate: MutableList<Invoice> = mutableListOf<Invoice>()

    override fun onMessage(message: Message?) {
        try {

            val mapMessage = message as ObjectMessage
            val invoiceMessage: Invoice = mapMessage.getObject() as Invoice
            logger.info { "This is info log : Inside subscriber3 : ${invoiceMessage.toString()}" }

            //Checking the amount should not be zero
            if (invoiceMessage.amount.value != BigDecimal.ZERO) {
                logger.info { "Inside my method : " }
                listOfToUpdate.add(invoiceMessage)

                //Billing the consumer as per Invoice
                BillingService(
                    paymentProvider = paymentProvider,
                    invoiceMessage,
                    invoiceService
                ).consumerPaymentCharge()
            }
            logger.info { "Final output: $listOfToUpdate" };
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: JMSException) {
            e.printStackTrace()
        }
    }

}
