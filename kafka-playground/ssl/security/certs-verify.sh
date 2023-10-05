#!/bin/bash

# Reference: https://github.com/confluentinc/cp-demo/blob/7.5.0-post/scripts/security

set -o nounset \
    -o errexit \
    -o verbose

# See what is in each keystore and truststore
for i in gateway kafka-client zookeeper
do
        echo "------------------------------- $i keystore -------------------------------"
	keytool -list -v -keystore kafka.$i.keystore.jks -storepass confluent | grep -e Alias -e Entry
        echo "------------------------------- $i truststore -------------------------------"
	keytool -list -v -keystore kafka.$i.truststore.jks -storepass confluent | grep -e Alias -e Entry
done