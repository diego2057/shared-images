package com.tul.shared.shared_images.service.image

import com.tul.shared.shared_images.model.Image
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CrudService {
    fun findAll(): Flux<Image>
    fun findById(id: String): Mono<Image>
    fun save(image: Image): Mono<Image>
    fun delete(image: Image): Mono<Void>
}
