package com.tul.shared.shared_images.service.gallery

import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest
import com.tul.shared.shared_images.model.Gallery
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CrudService {
    fun findAll(): Flux<Gallery>
    fun findById(uuid: String): Mono<Gallery>
    fun save(galleryRequest: GalleryRequest): Mono<Gallery>
}
