#!/bin/bash

BOOTSTRAP_SERVERS=kafka:10092

CONSUMER_CONFIG=/etc/kafka/client/consumer-ssl.properties

TOPIC=cars

TIMEOUT_MS=5000

docker exec -it kafka-client kafka-console-consumer \
  --bootstrap-server $BOOTSTRAP_SERVERS \
  --consumer.config $CONSUMER_CONFIG \
  --topic $TOPIC \
  --timeout-ms $TIMEOUT_MS
