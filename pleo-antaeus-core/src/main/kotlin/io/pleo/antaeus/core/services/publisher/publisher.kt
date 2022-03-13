package io.pleo.antaeus.core.services.publisher

import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.services.Constants.PRODUCER_TOPIC_NAME
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*

fun activeMQPublisher(invoiceService: InvoiceService) {

    val logger = KotlinLogging.logger {}

    try {
        logger.info { "Inside activeMQPublisher" }

        val pubConnectionFactory = ActiveMQConnectionFactory()
        pubConnectionFactory.isTrustAllPackages = true;
        val pubConnection = pubConnectionFactory.createConnection()
        val pubSession = pubConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val createProducerTopic = pubSession.createTopic(PRODUCER_TOPIC_NAME)
        val messageProducer = pubSession.createProducer(createProducerTopic)
        messageProducer.deliveryMode = DeliveryMode.PERSISTENT;

        //Send list of pending Invoices here
        val listOfInvoiceDetails = invoiceService.fetchAll();
        listOfInvoiceDetails.forEach { invoiceData ->
            //Sending only Pending status invoices
            if (invoiceData.status == InvoiceStatus.PENDING) {
                val msg = pubSession.createObjectMessage(invoiceData)
                messageProducer.send(msg)
                logger.info { "Inside activeMQPublisher : invoiceDetails : $invoiceData" }
            }
        }
        pubConnection.close()
    } catch (e: NetworkException) {
        logger.error { "Inside activeMQPublisher : Exception $e" }
        e.printStackTrace()
    } catch (e: Exception) {
        logger.error { "Inside activeMQPublisher : Exception $e" }
        e.printStackTrace()
    }
}

