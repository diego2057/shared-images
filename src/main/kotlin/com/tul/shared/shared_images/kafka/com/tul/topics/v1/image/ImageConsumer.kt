package com.tul.shared.shared_images.kafka.com.tul.topics.v1.image

import com.fasterxml.jackson.databind.ObjectMapper
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.MessageImage
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.service.image.CrudService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ImageConsumer(
    private val imageCrudService: CrudService,
    private val imageMapper: ImageMapper,
    private val objectMapper: ObjectMapper
) {
    @KafkaListener(topics = ["com.tul.shared.shared_images.v1.images.create"], groupId = "create")
    fun create(message: String) {
        val messageImage = objectMapper.readValue(message, MessageImage::class.java)
        imageCrudService.save(messageImage).subscribe()
    }

    @KafkaListener(topics = ["com.tul.shared.shared_images.v1.images.update"], groupId = "update")
    fun update(message: String) {
        val messageImage = objectMapper.readValue(message, MessageImage::class.java)
        imageCrudService.update(messageImage).subscribe()
    }

    private fun messageToImage(messageImage: MessageImage): Image {
        val image = imageMapper.toModel(messageImage.getImageRequest())
        image.mimeType = messageImage.mimeType
        image.fileName = messageImage.fileName
        return image
    }
}
