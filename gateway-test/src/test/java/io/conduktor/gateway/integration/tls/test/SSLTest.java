package io.conduktor.gateway.integration.tls.test;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.config.internals.BrokerSecurityConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;

import java.util.Properties;

public class SSLTest {

    @Test
    public void test() {
        // Define Kafka broker connection properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:6969");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        // Enable SSL/TLS communication
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "config/kafka.kafka-client.truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "confluent");

        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "config/kafka.kafka-client.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "confluent");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "confluent");

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {

            ProducerRecord<String, String> record = new ProducerRecord<>("cars", "key", "value");
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.println("Message sent successfully!");
                } else {
                    exception.printStackTrace();
                    System.err.println("Error sending message: " + exception.getMessage());
                }
            });
        }
    }
}
