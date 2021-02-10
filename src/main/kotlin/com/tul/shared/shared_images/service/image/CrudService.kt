package com.tul.shared.shared_images.service.image

import com.tul.shared.shared_images.model.Image
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.image.v1.ImageRequest as RestImageRequest

interface CrudService {
    fun findAll(): Flux<Image>
    fun findById(id: String): Mono<Image>
    fun save(imageRequest: RestImageRequest): Mono<Image>
    fun saveDefaultImage(image: Image, byteArray: ByteArray)
    fun update(imageRequest: RestImageRequest, id: String): Mono<Image>
    fun delete(id: String): Mono<Void>
}
