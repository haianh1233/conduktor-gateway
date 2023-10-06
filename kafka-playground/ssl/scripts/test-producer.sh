#!/bin/bash

BOOTSTRAP_SERVERS=kafka:10092

PRODUCER_CONFIG=/etc/kafka/client/producer-ssl.properties

TOPIC=cars

MESSAGE="This is a test message."

# Produce messages using kafka-console-producer
docker exec -i kafka-client kafka-console-producer \
  --bootstrap-server $BOOTSTRAP_SERVERS \
  --producer.config $PRODUCER_CONFIG \
  --topic $TOPIC <<EOF
$MESSAGE
EOF