package io.pleo.antaeus.core.services.publisher

import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*

fun activeMQPublisher(invoiceService: InvoiceService) {

    try {
        val logger = KotlinLogging.logger {}
        logger.info { "This is info log : Inside activeMQPublisher" }

        val pubConnectionFactory = ActiveMQConnectionFactory()
        pubConnectionFactory.isTrustAllPackages = true;
        val pubConnection = pubConnectionFactory.createConnection()
        val pubSession = pubConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val createProducerTopic = pubSession.createTopic("Pleo Producer Topic")
        val messageProducer = pubSession.createProducer(createProducerTopic)
        messageProducer.deliveryMode = DeliveryMode.PERSISTENT;

        //add the code to send messages here
        val listOfInvoiceDetails = invoiceService.fetchAll();
        logger.info { "This is info log : Inside activeMQPublisher listOfInvoiceDetails : $listOfInvoiceDetails" }
        listOfInvoiceDetails.forEach { invoiceData ->
            if (invoiceData.status == InvoiceStatus.PENDING) {
                val msg = pubSession.createObjectMessage(invoiceData)
                messageProducer.send(msg)
                logger.info { "This is info log : Inside activeMQPublisher invoiceDetails : $invoiceData" }
            }
        }
        pubConnection.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

