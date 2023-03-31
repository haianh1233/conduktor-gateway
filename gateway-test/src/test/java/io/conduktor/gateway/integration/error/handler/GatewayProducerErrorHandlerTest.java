/*
 * Copyright 2023 Conduktor, Inc
 *
 * Licensed under the Conduktor Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * https://www.conduktor.io/conduktor-community-license-agreement-v1.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.conduktor.gateway.integration.error.handler;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.UnknownServerException;
import org.apache.kafka.common.protocol.ApiKeys;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GatewayProducerErrorHandlerTest extends BaseErrorHandlerTest {

    private static Stream<Arguments> producerTestArguments() {
        return Stream.of(
                Arguments.of(ApiKeys.PRODUCE, ExceptionMockType.REQUEST, UnknownServerException.class),
                Arguments.of(ApiKeys.PRODUCE, ExceptionMockType.RESPONSE, UnknownServerException.class),
                Arguments.of(ApiKeys.METADATA, ExceptionMockType.REQUEST, TimeoutException.class),
                Arguments.of(ApiKeys.METADATA, ExceptionMockType.RESPONSE, TimeoutException.class),
                Arguments.of(ApiKeys.API_VERSIONS, ExceptionMockType.REQUEST, TimeoutException.class),
                Arguments.of(ApiKeys.API_VERSIONS, ExceptionMockType.RESPONSE, TimeoutException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("producerTestArguments")
    public void testProduce(ApiKeys apiKeys, ExceptionMockType type, Class<?> expectedException) throws ExecutionException, InterruptedException {
        mockException(type, apiKeys);
        var gatewayTopic = generateClientTopic();
        try (var gatewayProducer = clientFactory.gatewayProducer()) {
            var record = new ProducerRecord<>(gatewayTopic, KEY_1, VAL_1);
            assertThatThrownBy(() -> gatewayProducer.send(record).get(3, TimeUnit.SECONDS))
                    .cause()
                    .isInstanceOf(expectedException);
        }
    }

}
