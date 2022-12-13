package com.tul.shared.shared_images.kafka.images.v1

import com.tul.shared.shared_images.dto.Action
import com.tul.shared.shared_images.dto.image.v1.ImageKafkaRequest
import com.tul.shared.shared_images.service.image.ImageService
import com.tul.shared.shared_images.util.JsonUtil.Companion.asJsonString
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ImagesConsumer(
    private val imageService: ImageService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["v1.shared-images.images"], containerFactory = "config.kafka.image.consumerFactory")
    fun images(request: ImageKafkaRequest) {
        logger.debug("New image request - Kafka: ${request.asJsonString()}")

        when (request.action) {
            Action.create -> {
                logger.debug("Creating image - Kafka: ${request.body?.asJsonString()}")
                request.body?.let { this.imageService.saveOrUpdateFromUrl(it).block() }
                logger.debug("Image created from Kafka with UUID: ${request.body?.uuid}")
            }
            Action.update -> {
                logger.debug("Updating image - Kafka: ${request.body?.asJsonString()}")
                request.body?.let { this.imageService.saveOrUpdateFromUrl(it).block() }
                logger.debug("Image updated from Kafka with UUID: ${request.body?.uuid}")
            }
            else -> {
                logger.debug("Action to execute unknown: ${request.action}")
            }
        }
    }
}
