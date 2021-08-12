package com.tul.shared.shared_images.service.image.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.image.CrudRepository
import com.tul.shared.shared_images.service.image.ImageService
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service("image.crud_service")
class ImageServiceImpl(
    private val imageCrudRepository: CrudRepository,
    private val tinifyService: TinifyService,
    private val imageMapper: ImageMapper,
    private val webClient: WebClient
) : ImageService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun findAll(): Flux<Image> {
        return imageCrudRepository.findAll()
    }

    override fun findById(id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
            .switchIfEmpty(imageCrudRepository.findById(UUID(0, 0).toString()))
    }

    override fun save(imageRequest: CreateImageRequest): Mono<Image> {
        val image = imageMapper.toModel(imageRequest)
        val file = imageRequest.image!!
        image.fileName = file.filename()
        image.mimeType = file.headers().getFirst(HttpHeaders.CONTENT_TYPE)
        return imageCrudRepository.findById(imageRequest.uuid!!)
            .doOnNext { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The image with id ${imageRequest.uuid} already exists") }
            .switchIfEmpty(
                tinifyService.compressImage(file)
                    .flatMap { storeImage(image, it) }
            )
    }

    override fun saveOrUpdateFromUrl(imageUrlRequest: ImageUrlRequest): Mono<Image> {
        return imageCrudRepository.findById(imageUrlRequest.uuid!!)
            .doOnNext { imageMapper.updateModel(imageUrlRequest, it) }
            .switchIfEmpty(Mono.just(imageMapper.toModel(imageUrlRequest)))
            .flatMap { image ->
                getImageFromUrl(imageUrlRequest.url!!)
                    .flatMap { tinifyService.compressImage(it) }
                    .flatMap { storeImage(image, it) }
            }
    }

    override fun saveDefaultImage(image: Image, byteArray: ByteArray) {
        imageCrudRepository.findById(image.uuid)
            .switchIfEmpty(
                tinifyService.compressImage(byteArray)
                    .flatMap { storeImage(image, it) }
            ).subscribe()
    }

    override fun update(imageRequest: UpdateImageRequest, id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
            .switchIfEmpty(
                save(
                    CreateImageRequest().apply {
                        uuid = id
                        image = imageRequest.image
                    }
                )
            )
            .doOnNext { imageMapper.updateModel(imageRequest, it) }
            .flatMap {
                val file = imageRequest.image
                if (file != null) {
                    it.fileName = file.filename()
                    it.mimeType = file.headers().getFirst(HttpHeaders.CONTENT_TYPE)
                    tinifyService.compressImage(file).flatMap { json -> storeImage(it, json) }
                } else {
                    imageCrudRepository.save(it)
                }
            }
    }

    override fun delete(id: String): Mono<Void> {
        return imageCrudRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .flatMap { imageCrudRepository.delete(it) }
    }

    private fun storeImage(image: Image, jsonNode: JsonNode): Mono<Image> {
        image.size = jsonNode.get("input").get("size").asLong()
        return tinifyService.storeImage(jsonNode.get("output").get("url").textValue(), image.uuid)
            .flatMap {
                image.url = it
                imageCrudRepository.save(image)
            }
    }

    override fun findIndexMultiple(ids: List<String>): Flux<Image> {
        return imageCrudRepository.findByUuidIn(ids)
    }

    private fun getImageFromUrl(url: String): Mono<ByteArray> {
        return try {
            webClient.get()
                .uri(url)
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(ByteArray::class.java)
        } catch (e: Exception) {
            log.warn(e.message)
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY)
        }
    }
}
