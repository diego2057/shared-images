package com.tul.shared.shared_images.service.gallery

import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.model.Gallery
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest as RestGalleryRequest

interface CrudService {
    fun findAll(): Flux<Gallery>
    fun findById(uuid: String): Mono<Gallery>
    fun save(galleryRequest: RestGalleryRequest): Mono<Gallery>
    fun update(uuid: String, imageRequest: UpdateImageRequest): Mono<Gallery>
    fun deleteImage(uuid: String, imageUuid: String): Mono<Gallery>
}
