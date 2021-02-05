package com.tul.shared.shared_images.kafka.com.tul.topics.v1.image

import com.tul.shared.shared_images.config.kafka.KafkaConfig
import com.tul.shared.shared_images.config.kafka.ProducerFactory
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.KafkaProducerTopic
import com.tul.shared.shared_images.model.Image
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import java.util.UUID

@Component
class ImageProducer(
    kafkaProperties: KafkaProperties,
    producerFactory: ProducerFactory,
    private val imageMapper: ImageMapper
) {
    private val sender: KafkaSender<String, ImageDto>

    init {
        val senderOptions = SenderOptions.create<String, ImageDto>(KafkaConfig.getConfig(kafkaProperties))
        sender = KafkaSender.create(producerFactory, senderOptions)
    }

    fun sendMessage(image: Image, kafkaProducerTopic: KafkaProducerTopic) {
        val imageDto = imageMapper.toDto(image)
        sender.send(
            Mono.just(SenderRecord.create(ProducerRecord(kafkaProducerTopic.topic, imageDto), UUID.randomUUID()))
        ).subscribe()
    }
}
