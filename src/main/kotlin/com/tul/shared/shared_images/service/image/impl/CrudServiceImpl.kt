package com.tul.shared.shared_images.service.image.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.image.CrudRepository
import com.tul.shared.shared_images.service.image.CrudService
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CrudServiceImpl(
    private val imageCrudRepository: CrudRepository,
    private val tinifyService: TinifyService
) : CrudService {

    override fun findAll(): Flux<Image> {
        return imageCrudRepository.findAll()
    }

    override fun findById(id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
    }

    override fun save(image: Image, imageFilePart: FilePart?): Mono<Image> {
        return if (imageFilePart != null) {
            image.fileName = imageFilePart.filename()
            image.mimeType = imageFilePart.headers().getFirst("Content-Type")!!
            compressImage(imageFilePart)
                .doOnNext { image.size = it.get("input").get("size").asLong() }
                .flatMap { tinifyService.storeImage(it.get("output").get("url").textValue(), image.fileName!!) }
                .flatMap {
                    image.url = it
                    imageCrudRepository.save(image)
                }
                .next()
        } else {
            imageCrudRepository.save(image)
        }
    }

    override fun delete(image: Image): Mono<Void> {
        return imageCrudRepository.delete(image)
    }

    private fun compressImage(imageFilePart: FilePart): Flux<JsonNode> {
        return imageFilePart.content()
            .map { dataBuffer ->
                val byteArray = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(byteArray)
                return@map byteArray
            }.flatMap { tinifyService.compressImage(it) }
    }
}
