package com.tul.shared.shared_images.kafka.com.tul.topics.v1.gallery

import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.KafkaProducerTopic
import com.tul.shared.shared_images.model.Gallery
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.util.UUID

@Component
class GalleryProducer(
    @Value("\${spring.kafka.producer.bootstrap-servers}")
    private val bootstrapServer: String,
    private val galleryMapper: GalleryMapper,
) {
    private val sender: KafkaSender<String, GalleryDto>

    init {
        val props = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, GalleryDto>(props)
        sender = KafkaSender.create(senderOptions)
    }

    fun sendMessage(gallery: Gallery, kafkaProducerTopic: KafkaProducerTopic) {
        val galleryDto = galleryMapper.toDto(gallery)
        sender.send(
            Mono.just(SenderRecord.create(ProducerRecord(kafkaProducerTopic.topic, galleryDto), UUID.randomUUID()))
        ).subscribe()
    }
}
