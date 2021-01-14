package com.tul.shared.shared_images.configuration

import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest
import com.tul.shared.shared_images.service.image.CrudService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class ImageConfiguration(
    @Value("\${app.default-image-id}")
    private val defaultImageId: String,
    private val imageCrudService: CrudService
) : ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val file = ClassPathResource("default.jpeg")

        val imageRequest = ImageRequest().apply {
            uuid = defaultImageId
            title = "default"
            fileName = file.filename
            mimeType = MediaType.IMAGE_JPEG_VALUE
            byteArray = file.file.readBytes()
        }

        imageCrudService.findById(defaultImageId)
            .switchIfEmpty(imageCrudService.save(imageRequest))
            .subscribe()
    }
}
