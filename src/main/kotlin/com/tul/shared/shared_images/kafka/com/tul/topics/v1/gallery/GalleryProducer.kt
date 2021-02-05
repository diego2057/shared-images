package com.tul.shared.shared_images.kafka.com.tul.topics.v1.gallery

import com.tul.shared.shared_images.config.kafka.KafkaConfig
import com.tul.shared.shared_images.config.kafka.ProducerFactory
import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.KafkaProducerTopic
import com.tul.shared.shared_images.model.Gallery
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.util.UUID

@Component
class GalleryProducer(
    kafkaProperties: KafkaProperties,
    producerFactory: ProducerFactory,
    private val galleryMapper: GalleryMapper,
) {
    private val sender: KafkaSender<String, GalleryDto>

    init {
        val senderOptions = SenderOptions.create<String, GalleryDto>(KafkaConfig.getConfig(kafkaProperties))
        sender = KafkaSender.create(producerFactory, senderOptions)
    }

    fun sendMessage(gallery: Gallery, kafkaProducerTopic: KafkaProducerTopic) {
        val galleryDto = galleryMapper.toDto(gallery)
        sender.send(
            Mono.just(SenderRecord.create(ProducerRecord(kafkaProducerTopic.topic, galleryDto), UUID.randomUUID()))
        ).subscribe()
    }
}
