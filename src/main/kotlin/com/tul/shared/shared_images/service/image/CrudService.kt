package com.tul.shared.shared_images.service.image

import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.model.Image
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CrudService {
    fun findAll(): Flux<Image>
    fun findById(id: String): Mono<Image>
    fun save(imageRequest: CreateImageRequest): Mono<Image>
    fun saveDefaultImage(image: Image, byteArray: ByteArray)
    fun update(imageRequest: UpdateImageRequest, id: String): Mono<Image>
    fun delete(id: String): Mono<Void>
    fun findIndexMultiple(ids: List<String>): Flux<Image>
}
