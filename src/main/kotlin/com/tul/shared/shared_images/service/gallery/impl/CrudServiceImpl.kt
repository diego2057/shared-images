package com.tul.shared.shared_images.service.gallery.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.model.Gallery
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.gallery.CrudRepository
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest as RestGalleryRequest
import com.tul.shared.shared_images.service.gallery.CrudService as GalleryService

@Service("gallery.crud_service")
class CrudServiceImpl(
    private val galleryRepository: CrudRepository,
    private val tinifyService: TinifyService,
    private val imageMapper: ImageMapper,
    private val galleryMapper: GalleryMapper,
) : GalleryService {

    override fun findAll(): Flux<Gallery> {
        return galleryRepository.findAll()
    }

    override fun findById(uuid: String): Mono<Gallery> {
        return galleryRepository.findById(uuid)
            .switchIfEmpty(Mono.just(Gallery(uuid, mutableListOf())))
    }

    override fun save(galleryRequest: RestGalleryRequest): Mono<Gallery> {
        val gallery = galleryMapper.toModel(galleryRequest)
        return Flux.fromIterable(galleryRequest.images.asIterable())
            .flatMap { imageRequest ->
                val image = imageMapper.toModel(imageRequest)
                val file = imageRequest.image!!
                image.fileName = file.filename()
                image.mimeType = file.headers().getFirst("Content-Type")
                tinifyService.compressImage(file).flatMap { storeImage(image, it) }
            }
            .collectList()
            .doOnNext { gallery.images = it }
            .thenReturn(gallery)
            .flatMap { galleryRepository.save(it) }
    }

    override fun update(uuid: String, imageRequest: UpdateImageRequest): Mono<Gallery> {
        val monoImage = Mono.just(imageRequest).map(imageMapper::toModel)
            .flatMap {
                val file = imageRequest.image!!
                it.fileName = file.filename()
                it.mimeType = file.headers().getFirst("Content-Type")
                tinifyService.compressImage(file).flatMap { json -> storeImage(it, json) }
            }

        return galleryRepository.findById(uuid)
            .zipWith(monoImage)
            .doOnNext { it.t1.images?.add(it.t2) }
            .flatMap { galleryRepository.save(it.t1) }
    }

    override fun deleteImage(uuid: String, imageUuid: String): Mono<Gallery> {
        return galleryRepository.findById(uuid)
            .flatMap {
                it.images?.removeIf { image -> image.uuid == imageUuid }
                galleryRepository.save(it)
            }
    }

    private fun storeImage(image: Image, jsonNode: JsonNode): Mono<Image> {
        image.size = jsonNode.get("input").get("size").asLong()
        return tinifyService.storeImage(jsonNode.get("output").get("url").textValue(), image.fileName!!)
            .doOnNext { image.url = it }
            .thenReturn(image)
    }
}
