package com.tul.shared.shared_images.kafka.galleries.v1

import com.tul.shared.shared_images.dto.Action
import com.tul.shared.shared_images.dto.gallery.v1.GalleryKafkaRequest
import com.tul.shared.shared_images.service.gallery.GalleryService
import com.tul.shared.shared_images.util.JsonUtil.Companion.asJsonString
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GalleriesConsumer(
    private val galleryService: GalleryService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["v1.shared-images.galleries"], containerFactory = "config.kafka.gallery.consumerFactory")
    fun galleries(request: GalleryKafkaRequest) {
        logger.debug("New gallery request - Kafka: ${request.asJsonString()}")

        when (request.action) {
            Action.create -> {
                logger.debug("Creating gallery - Kafka: ${request.body?.asJsonString()}")
                request.body?.let { it.uuid?.let { it1 -> this.galleryService.saveFromUrl(it1, it.images).block() } }
                logger.debug("Gallery created from Kafka with UUID: ${request.body?.uuid}")
            }
            Action.update -> {
                logger.debug("Updating gallery - Kafka: ${request.body?.asJsonString()}")
                request.body?.let { it.uuid?.let { it1 -> this.galleryService.updateFromUrl(it1, it.images).block() } }
                logger.debug("Gallery updated from Kafka with UUID: ${request.body?.uuid}")
            }
            else -> {
                logger.debug("Action to execute unknown: ${request.action}")
            }
        }
    }
}
