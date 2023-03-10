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
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest as RestGalleryRequest
import com.tul.shared.shared_images.service.gallery.GalleryService as GalleryService

@Service("gallery.crud_service")
class GalleryServiceImpl(
    private val galleryRepository: CrudRepository,
    private val tinifyService: TinifyService,
    private val imageMapper: ImageMapper,
    private val galleryMapper: GalleryMapper,
    private val webClient: WebClient
) : GalleryService {

    private val log = LoggerFactory.getLogger(javaClass)

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
                image.fileName = file.originalFilename
                image.mimeType = file.contentType
                tinifyService.compressImage(file.bytes).flatMap { storeImage(image, it, gallery.uuid) }
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
                it.fileName = file.originalFilename
                it.mimeType = file.contentType
                tinifyService.compressImage(file.bytes).flatMap { json -> storeImage(it, json, uuid) }
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
                        image.fileName = file.originalFilename
                        image.mimeType = file.contentType
                        monoImage = tinifyService.compressImage(file.bytes).flatMap { json -> storeImage(image, json, gallery.uuid) }
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

    override fun saveFromUrl(uuid: String, images: List<ImageUrlRequest>): Mono<Gallery> {
        return Flux.fromIterable(images.asIterable())
            .flatMap { Mono.just(imageMapper.toModel(it)) }
            .flatMap { image ->
                getImageFromUrl(image.url!!)
                    .flatMap { tinifyService.compressImage(it) }
                    .flatMap { storeImage(image, it, uuid) }
                    .toMono()
            }
            .collectList()
            .map { Gallery(uuid, it) }
            .flatMap(galleryRepository::save)
    }

    override fun updateFromUrl(uuid: String, images: List<ImageUrlRequest>): Mono<Gallery> {
        val galleryMono = galleryRepository.findById(uuid).defaultIfEmpty(Gallery(uuid, mutableListOf()))
        val imageFlux = Flux.fromIterable(images.asIterable())

        return galleryMono.flatMap { gallery ->
            imageFlux.flatMap { imageRequest ->
                var image = imageRequest.uuid?.let { gallery.images.find { image -> image.uuid == it } }

                if (image == null && imageRequest.url !== null) {
                    image = imageMapper.toModel(imageRequest)
                    gallery.images.add(image)
                }

                var monoImage = Mono.justOrEmpty(image)

                if (image != null) {
                    imageMapper.updateModel(imageRequest, image)
                    monoImage = getImageFromUrl(image.url!!)
                        .flatMap { tinifyService.compressImage(it) }
                        .flatMap { storeImage(image, it, gallery.uuid) }
                        .toMono()
                }

                monoImage
            }
                .then(Mono.just(gallery))
                .flatMap(galleryRepository::save)
        }
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

    private fun getImageFromUrl(url: String): Mono<ByteArray> {
        return try {
            webClient.get()
                .uri(url)
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(ByteArray::class.java)
        } catch (e: Exception) {
            log.debug(e.message)
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY)
        }
    }
}
