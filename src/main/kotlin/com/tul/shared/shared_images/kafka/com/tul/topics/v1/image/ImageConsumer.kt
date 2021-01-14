package com.tul.shared.shared_images.kafka.com.tul.topics.v1.image

import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest
import com.tul.shared.shared_images.service.image.CrudService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ImageConsumer(
    private val imageCrudService: CrudService,
) {
    @KafkaListener(topics = ["com.tul.shared.shared_images.v1.images.create"], containerFactory = "messageImageKafkaListenerContainerFactory")
    fun create(imageRequest: ImageRequest) {
        imageCrudService.save(imageRequest).subscribe()
    }

    @KafkaListener(topics = ["com.tul.shared.shared_images.v1.images.update"], containerFactory = "messageImageKafkaListenerContainerFactory")
    fun update(messageImage: ImageRequest) {
        imageCrudService.update(messageImage).subscribe()
    }
}
