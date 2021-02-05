package com.tul.shared.shared_images.kafka.com.tul.topics.v1.gallery

import com.tul.shared.shared_images.dto.gallery.v1.kafka.GalleryRequest
import com.tul.shared.shared_images.service.gallery.CrudService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GalleryConsumer(
    private val galleryCrudService: CrudService
) {
    @KafkaListener(topics = ["com.tul.shared.shared_images.v1.gallery.create"], containerFactory = "config.kafka.gallery.consumerFactory")
    fun create(galleryRequest: GalleryRequest) {
        galleryCrudService.save(galleryRequest).subscribe()
    }
}
