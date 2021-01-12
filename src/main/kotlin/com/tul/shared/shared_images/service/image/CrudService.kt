package com.tul.shared.shared_images.service.image

import com.tul.shared.shared_images.dto.image.v1.ImageRequest
import com.tul.shared.shared_images.dto.image.v1.MessageImage
import com.tul.shared.shared_images.model.Image
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CrudService {
    fun findAll(): Flux<Image>
    fun findById(id: String): Mono<Image>
    fun save(imageRequest: ImageRequest): Mono<Image>
    fun save(messageImage: MessageImage): Mono<Image>
    fun update(imageRequest: ImageRequest, id: String): Mono<Image>
    fun update(messageImage: MessageImage): Mono<Image>
    fun delete(id: String): Mono<Void>
}
