package com.tul.shared.shared_images.config.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaConfig(
    private val objectMapper: ObjectMapper
) {

    companion object {
        fun getConfig(kafkaProperties: KafkaProperties): Map<String, Any> {
            return mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to kafkaProperties.consumer.groupId,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers
            )
        }
    }

    @Bean
    fun <T> kafkaTemplate(userFactory: ProducerFactory<String, T>): KafkaTemplate<String, T> {
        return KafkaTemplate(userFactory)
    }

    @Bean
    fun <T> producerFactory(kafkaProperties: KafkaProperties): ProducerFactory<String, T> {
        val valueSerializer: JsonSerializer<T> = JsonSerializer(objectMapper)
        return DefaultKafkaProducerFactory(
            getConfig(kafkaProperties),
            StringSerializer(),
            valueSerializer.noTypeInfo()
        )
    }
}
