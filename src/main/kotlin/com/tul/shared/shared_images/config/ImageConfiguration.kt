package com.tul.shared.shared_images.config

import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest
import com.tul.shared.shared_images.service.image.CrudService
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ImageConfiguration(
    private val imageCrudService: CrudService
) : ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val file = ClassPathResource("default.jpeg")

        val defaultImageId = UUID(0, 0).toString()
        val imageRequest = ImageRequest(defaultImageId).apply {
            title = "default"
            fileName = file.filename
            mimeType = MediaType.IMAGE_JPEG_VALUE
            byteArray = file.inputStream.readAllBytes()
        }

        imageCrudService.findById(defaultImageId)
            .switchIfEmpty(imageCrudService.save(imageRequest))
            .subscribe()
    }
}
