package io.conduktor.gateway.integration.tls;

import org.apache.kafka.clients.producer.*;
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
        props.put("security.protocol", "SSL");
        props.put("ssl.truststore.location", "config/client-1-truststore.jks");
        props.put("ssl.truststore.password", "thisispassword");
        props.put("ssl.keystore.location", "config/client-1.jks");
        props.put("ssl.keystore.password", "thisispassword");
        props.put("ssl.key.password", "thisispassword");

        Producer<String, String> producer = new KafkaProducer<>(props);

        ProducerRecord<String, String> record = new ProducerRecord<>("cars", "key", "value");
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
                    System.out.println("Message sent successfully!");
                } else {
                    System.err.println("Error sending message: " + exception.getMessage());
                }
            }
        });

        // Close the Kafka producer
        producer.close();
    }
}
