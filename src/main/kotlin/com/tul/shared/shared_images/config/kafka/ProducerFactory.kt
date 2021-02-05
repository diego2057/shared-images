package com.tul.shared.shared_images.config.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.internals.ProducerFactory

@Component
class ProducerFactory(private val objectMapper: ObjectMapper) : ProducerFactory() {

    override fun <K, V> createProducer(senderOptions: SenderOptions<K, V>): Producer<K, V> {
        val valueSerializer: JsonSerializer<V> = JsonSerializer(objectMapper)
        return KafkaProducer(
            senderOptions.producerProperties(),
            senderOptions.keySerializer(),
            valueSerializer.noTypeInfo()
        )
    }
}
