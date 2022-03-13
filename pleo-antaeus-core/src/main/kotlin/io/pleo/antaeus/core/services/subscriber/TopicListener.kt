package io.pleo.antaeus.core.services.subscriber

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
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
    var update: Boolean = false;

    override fun onMessage(message: Message?) {
        try {

            val mapMessage = message as ObjectMessage
            val invoiceMessage: Invoice = mapMessage.getObject() as Invoice

            //Checking the amount should not be zero
            if (invoiceMessage.amount.value != BigDecimal.ZERO) {
                listOfInvoiceToUpdate.add(invoiceMessage)

                try {
                    //Billing the consumer as per Invoice
                    update = BillingService(
                        paymentProvider = paymentProvider,
                        invoiceMessage,
                        invoiceService
                    ).consumerPaymentCharge()
                    //logger.info{"Update value $update"}
                } catch (e: CurrencyMismatchException) {
                    logger.error { "Inside TopicListener : consumerPaymentCharge : Exception $e" }
                    e.printStackTrace()
                } catch (e: CustomerNotFoundException) {
                    logger.error { "Inside TopicListener : consumerPaymentCharge : Exception $e" }
                    e.printStackTrace()
                } catch (e: NetworkException) {
                    logger.error { "Inside TopicListener : consumerPaymentCharge : Exception $e" }
                    e.printStackTrace()
                }

                //Purging successfully charged invoices
                if (update) {
                    message.jmsExpiration = 60000 //1 hour
                } else {
                    //keep the uncharged invoice messages in the topic for further analysis logic
                    //My solution:
                    //when the scheduler runs the next time, which is after one hour, it will pull the pending messages again from the database.
                    //now when the chargeInvoice returns turn -> compare the invoiceID & customerID with the List of unprocessed messages stored
                    //If they are same -> purge
                    //If not they need to analysed further or for wait for the next schedule run
                    //There is no need to browse the unprocessed messages as in queue, In topics you always have a copy of the messages.
                }
            }
            logger.info { "Inside TopicListener: onMessage listOfInvoiceToUpdate = $listOfInvoiceToUpdate" }
        } catch (e: NumberFormatException) {
            logger.error { "Inside TopicListener : Exception $e" }
            e.printStackTrace()
        } catch (e: JMSException) {
            logger.error { "Inside TopicListener : Exception $e" }
            e.printStackTrace()
        } catch (e: Exception) {
            logger.error { "Inside TopicListener : Exception $e" }
            e.printStackTrace()
        }
    }

}
