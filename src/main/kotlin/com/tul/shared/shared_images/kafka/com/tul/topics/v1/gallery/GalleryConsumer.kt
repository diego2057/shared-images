package com.tul.shared.shared_images.kafka.com.tul.topics.v1.gallery

import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.gallery.v1.kafka.GalleryRequest
import com.tul.shared.shared_images.service.gallery.CrudService
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class GalleryConsumer(
    @Value("\${spring.kafka.producer.bootstrap-servers}")
    private val bootstrapServer: String,
    private val galleryMapper: GalleryMapper,
    private val galleryCrudService: CrudService
) {
    @KafkaListener(topics = ["com.tul.shared.shared_images.v1.images.create"], containerFactory = "messageImageKafkaListenerContainerFactory")
    fun create(galleryRequest: GalleryRequest) {
        galleryCrudService.save(galleryRequest).subscribe()
    }
}
