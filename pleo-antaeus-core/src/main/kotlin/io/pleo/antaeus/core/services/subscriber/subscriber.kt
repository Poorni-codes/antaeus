package io.pleo.antaeus.core.services.subscriber

import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.Constants.CLIENT_ID
import io.pleo.antaeus.core.services.Constants.CONSUMER_TOPIC_NAME
import io.pleo.antaeus.core.services.Constants.DURABLE_SUBSCRIBER_NAME
import io.pleo.antaeus.core.services.InvoiceService
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*

suspend fun activeMQSubscriber(paymentProvider: PaymentProvider, invoiceService: InvoiceService) {

    val logger = KotlinLogging.logger {}

    try {
        //Delaying just to be on the safer side
        //delay(1000L)
        logger.info { "Inside subscriber" }

        val subConnectionFactory = ActiveMQConnectionFactory()
        subConnectionFactory.isTrustAllPackages = true;
        val subConnection = subConnectionFactory.createConnection()
        subConnection.clientID = CLIENT_ID
        val subSession = subConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val createSubscriberTopic = subSession.createTopic(CONSUMER_TOPIC_NAME)

        //createDurableSubscriber is used so no messages sent are lost in service unavailable cases
        val messageConsumer: MessageConsumer =
            subSession.createDurableSubscriber(createSubscriberTopic, DURABLE_SUBSCRIBER_NAME)

        subConnection.start()

        //Asynchronously listen to producer topic
        while (messageConsumer != null) {
            messageConsumer.messageListener = TopicListener(paymentProvider, invoiceService);
        }

        subConnection.close()

    } catch (e: NetworkException) {
        logger.error { "Inside activeMQSubscriber : Exception $e" }
        e.printStackTrace()
    } catch (e: Exception) {
        logger.error { "Inside activeMQSubscriber : Exception $e" }
        e.printStackTrace()
    }
}