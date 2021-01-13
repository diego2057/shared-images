package com.tul.shared.shared_images.service.gallery

import com.tul.shared.shared_images.model.Gallery
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.gallery.v1.kafka.GalleryRequest as KafkaGalleryRequest
import com.tul.shared.shared_images.dto.gallery.v1.rest.GalleryRequest as RestGalleryRequest

interface CrudService {
    fun findAll(): Flux<Gallery>
    fun findById(uuid: String): Mono<Gallery>
    fun save(galleryRequest: RestGalleryRequest): Mono<Gallery>
    fun save(galleryRequest: KafkaGalleryRequest): Mono<Gallery>
}
