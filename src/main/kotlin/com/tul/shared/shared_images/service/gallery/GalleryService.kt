package com.tul.shared.shared_images.service.gallery

import com.tul.shared.shared_images.dto.gallery.v1.GalleryImagesRequest
import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.model.Gallery
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest as RestGalleryRequest

interface GalleryService {
    fun findAll(): Flux<Gallery>
    fun findById(uuid: String): Mono<Gallery>
    fun save(galleryRequest: RestGalleryRequest): Mono<Gallery>
    fun addImage(uuid: String, imageRequest: UpdateImageRequest): Mono<Gallery>
    fun addImage(uuid: String, imageRequest: ImageUrlRequest): Mono<Gallery>
    fun update(uuid: String, images: List<CreateImageRequest>): Mono<Gallery>
    fun deleteImages(uuid: String, imagesUuid: List<String>): Mono<Gallery>
    fun deleteImage(uuid: String, imageUuid: String): Mono<Gallery>
    fun save(uuid: String, galleryRequest: GalleryImagesRequest): Mono<Gallery>
    fun multiple(ids: List<String>): Flux<Gallery>
}
