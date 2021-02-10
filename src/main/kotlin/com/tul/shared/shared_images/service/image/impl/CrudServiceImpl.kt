package com.tul.shared.shared_images.service.image.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.image.CrudRepository
import com.tul.shared.shared_images.service.image.CrudService
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service("image.crud_service")
class CrudServiceImpl(
    private val imageCrudRepository: CrudRepository,
    private val tinifyService: TinifyService,
    private val imageMapper: ImageMapper
) : CrudService {

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
        image.mimeType = file.headers().getFirst("Content-Type")
        return imageCrudRepository.findById(imageRequest.uuid!!)
            .doOnNext { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The image with id ${imageRequest.uuid} already exists") }
            .switchIfEmpty(
                tinifyService.compressImage(file)
                    .flatMap { storeImage(image, it) }
            )
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
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .doOnNext { imageMapper.updateModel(imageRequest, it) }
            .flatMap {
                val file = imageRequest.image
                if (file != null) {
                    it.fileName = file.filename()
                    it.mimeType = file.headers().getFirst("Content-Type")
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
        return tinifyService.storeImage(jsonNode.get("output").get("url").textValue(), image.fileName!!)
            .flatMap {
                image.url = it
                imageCrudRepository.save(image)
            }
    }
}
