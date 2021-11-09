package com.tul.shared.shared_images.service.gallery.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.dto.gallery.v1.GalleryImagesRequest
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.model.Gallery
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.gallery.CrudRepository
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest as RestGalleryRequest
import com.tul.shared.shared_images.service.gallery.GalleryService as GalleryService

@Service("gallery.crud_service")
class GalleryServiceImpl(
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
                image.mimeType = file.headers().getFirst(HttpHeaders.CONTENT_TYPE)
                tinifyService.compressImage(file).flatMap { storeImage(image, it, gallery.uuid) }
            }
            .collectList()
            .doOnNext { gallery.images = it }
            .thenReturn(gallery)
            .flatMap { galleryRepository.save(it) }
    }

    override fun save(uuid: String, galleryRequest: GalleryImagesRequest): Mono<Gallery> {
        return save(RestGalleryRequest(uuid, galleryRequest.images))
    }

    override fun addImage(uuid: String, imageRequest: UpdateImageRequest): Mono<Gallery> {
        return Mono.just(imageRequest).map(imageMapper::toModel)
            .flatMap {
                val file = imageRequest.image!!
                it.fileName = file.filename()
                it.mimeType = file.headers().getFirst(HttpHeaders.CONTENT_TYPE)
                tinifyService.compressImage(file).flatMap { json -> storeImage(it, json, uuid) }
            }
            .zipWith(galleryRepository.findById(uuid).defaultIfEmpty(Gallery(uuid, mutableListOf())))
            .doOnNext { it.t2.images.add(it.t1) }
            .flatMap { galleryRepository.save(it.t2) }
    }

    override fun addImage(uuid: String, imageRequest: ImageUrlRequest): Mono<Gallery> {
        return Mono.just(imageRequest).map(imageMapper::toModel)
            .zipWith(galleryRepository.findById(uuid).defaultIfEmpty(Gallery(uuid, mutableListOf())))
            .doOnNext { it.t2.images.add(it.t1) }
            .flatMap { galleryRepository.save(it.t2) }
    }

    override fun update(uuid: String, images: List<CreateImageRequest>): Mono<Gallery> {
        val galleryMono = galleryRepository.findById(uuid).defaultIfEmpty(Gallery(uuid, mutableListOf()))
        val imageFlux = Flux.fromStream(images.stream())
        return galleryMono.flatMap { gallery ->
            imageFlux.flatMap { imageRequest ->
                var image = imageRequest.uuid?.let { gallery.images.find { image -> image.uuid == it } }
                if (image == null && imageRequest.image != null) {
                    image = imageMapper.toModel(imageRequest)
                    gallery.images.add(image)
                }
                var monoImage = Mono.justOrEmpty(image)
                if (image != null) {
                    imageMapper.updateModel(imageRequest, image)
                    val file = imageRequest.image
                    if (file != null) {
                        image.fileName = file.filename()
                        image.mimeType = file.headers().getFirst(HttpHeaders.CONTENT_TYPE)
                        monoImage = tinifyService.compressImage(file).flatMap { json -> storeImage(image, json, gallery.uuid) }
                    }
                }
                monoImage
            }
                .then(Mono.just(gallery))
                .flatMap(galleryRepository::save)
        }
    }

    override fun deleteImages(uuid: String, imagesUuid: List<String>): Mono<Gallery> {
        return galleryRepository.findById(uuid)
            .flatMap {
                it.images.removeIf { image -> imagesUuid.contains(image.uuid) }
                galleryRepository.save(it)
            }
    }

    override fun deleteImage(uuid: String, imageUuid: String): Mono<Gallery> {
        return deleteImages(uuid, listOf(imageUuid))
    }

    override fun multiple(ids: List<String>): Flux<Gallery> {
        return galleryRepository.findByUuidIn(ids)
    }

    private fun storeImage(image: Image, jsonNode: JsonNode, galleryUUID: String): Mono<Image> {
        image.size = jsonNode.get("input").get("size").asLong()
        return tinifyService.storeImage(jsonNode.get("output").get("url").textValue(), extensionName(image, galleryUUID))
            .doOnNext { image.url = it }
            .thenReturn(image)
    }

    private fun extensionName(image: Image, galleryUUID: String): String {
        val extensionIndex = image.fileName!!.lastIndexOf('.')
        val extension = if (extensionIndex > 0) image.fileName!!.substring(extensionIndex) else ""
        return "${image.uuid}-$galleryUUID$extension"
    }
}
