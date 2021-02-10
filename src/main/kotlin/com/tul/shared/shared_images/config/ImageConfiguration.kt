package com.tul.shared.shared_images.config

import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.service.image.CrudService
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ImageConfiguration(
    private val imageCrudService: CrudService,
    private val imageMapper: ImageMapper
) : ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val file = ClassPathResource("default.jpeg")

        val defaultImageId = UUID(0, 0).toString()
        val image = imageMapper.toModel(
            CreateImageRequest().apply {
                uuid = defaultImageId
                title = "default"
            }
        )

        image.mimeType = MediaType.IMAGE_JPEG_VALUE
        image.fileName = file.filename

        imageCrudService.saveDefaultImage(image, file.inputStream.readAllBytes())
    }
}
