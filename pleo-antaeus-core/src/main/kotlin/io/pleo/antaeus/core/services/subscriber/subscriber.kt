package io.pleo.antaeus.core.services.subscriber

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.InvoiceService
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.*

suspend fun activeMQSubscriber(paymentProvider: PaymentProvider, invoiceService: InvoiceService) {
    try {

        //Delaying just to be on the safer side
        delay(2000L)

        val logger = KotlinLogging.logger {}
        logger.info { "This is info log : Inside subscriber" }

        val subConnectionFactory = ActiveMQConnectionFactory()
        subConnectionFactory.isTrustAllPackages = true;
        val subConnection = subConnectionFactory.createConnection()
        subConnection.clientID = "SampleClient"
        val subSession = subConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val createSubscriberTopic = subSession.createTopic("Pleo Producer Topic")
        val messageConsumer: MessageConsumer =
            subSession.createDurableSubscriber(createSubscriberTopic, "SampleSubscription")

        logger.info { "This is info log : Inside subscriber $messageConsumer" }
        subConnection.start()

        //Asynchronously listen to topic
        while (messageConsumer != null) {
            messageConsumer.messageListener = TopicListener("Consumer", paymentProvider, invoiceService);
        }

        logger.info { "This is info log : Inside subscriber90 $messageConsumer" }
        subConnection.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}