package com.tul.shared.shared_images.kafka.com.tul.topics.v1.image

import com.fasterxml.jackson.databind.ObjectMapper
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.model.Image
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.util.UUID

@Component
class ImageProducer(
    @Value("\${spring.kafka.producer.bootstrap-servers}")
    private val BOOTSTRAP_SERVERS: String,
    private val imageMapper: ImageMapper,
    private val objectMapper: ObjectMapper
) {
    private val sender: KafkaSender<String, String>

    init {
        val props = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, String>(props)
        sender = KafkaSender.create(senderOptions)
    }

    fun sendMessage(image: Image, kafkaImageProducerTopic: KafkaImageProducerTopic) {
        val imageDto = imageMapper.toDto(image)
        val mappedDto: String = objectMapper.writeValueAsString(imageDto)
        sender.send(
            Mono.just(SenderRecord.create(ProducerRecord(kafkaImageProducerTopic.topic, mappedDto), UUID.randomUUID()))
        ).subscribe()
    }
}
