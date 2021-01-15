package com.tul.shared.shared_images.service.image.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.KafkaProducerTopic
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.image.ImageProducer
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.image.CrudRepository
import com.tul.shared.shared_images.service.image.CrudService
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest as KafkaImageRequest
import com.tul.shared.shared_images.dto.image.v1.rest.ImageRequest as RestImageRequest

@Service("image.crud_service")
class CrudServiceImpl(
    private val imageCrudRepository: CrudRepository,
    private val tinifyService: TinifyService,
    private val imageProducer: ImageProducer,
    private val imageMapper: ImageMapper
) : CrudService {

    override fun findAll(): Flux<Image> {
        return imageCrudRepository.findAll()
    }

    override fun findById(id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
            .switchIfEmpty(imageCrudRepository.findById(UUID(0, 0).toString()))
    }

    override fun save(imageRequest: RestImageRequest): Mono<Image> {
        val image = imageMapper.toModel(imageRequest)
        val file = imageRequest.image!!
        image.fileName = file.filename()
        image.mimeType = file.headers().getFirst("Content-Type")
        return compressFilePartImage(file)
            .flatMap { storeImage(image, it) }
            .doOnNext { imageProducer.sendMessage(it, KafkaProducerTopic.CREATED_IMAGE) }
    }

    override fun save(messageImage: KafkaImageRequest): Mono<Image> {
        val image = imageMapper.toModelFromMessage(messageImage)
        return tinifyService.compressImage(messageImage.byteArray!!)
            .flatMap { storeImage(image, it) }
            .doOnNext { imageProducer.sendMessage(it, KafkaProducerTopic.CREATED_IMAGE) }
    }

    override fun update(imageRequest: RestImageRequest, id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .doOnNext { imageMapper.updateModel(imageMapper.toDtoFromRequest(imageRequest), it) }
            .flatMap {
                val file = imageRequest.image
                if (file != null) {
                    it.fileName = file.filename()
                    it.mimeType = file.headers().getFirst("Content-Type")
                    compressFilePartImage(file).flatMap { json -> storeImage(it, json) }
                } else {
                    imageCrudRepository.save(it)
                }
            }.doOnNext { imageProducer.sendMessage(it, KafkaProducerTopic.UPDATED_IMAGE) }
    }

    override fun update(messageImage: KafkaImageRequest): Mono<Image> {
        return imageCrudRepository.findById(messageImage.uuid)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .doOnNext {
                val imageReceived = imageMapper.toModelFromMessage(messageImage)
                imageMapper.updateModel(imageMapper.toDto(imageReceived), it)
            }.flatMap {
                val byteArray = messageImage.byteArray
                if (byteArray != null) {
                    tinifyService.compressImage(byteArray).flatMap { json -> storeImage(it, json) }
                } else {
                    imageCrudRepository.save(it)
                }
            }.doOnNext { imageProducer.sendMessage(it, KafkaProducerTopic.UPDATED_IMAGE) }
    }

    override fun delete(id: String): Mono<Void> {
        return imageCrudRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .flatMap { imageCrudRepository.delete(it) }
    }

    private fun compressFilePartImage(imageFilePart: FilePart): Mono<JsonNode> {
        return imageFilePart.content()
            .map { dataBuffer ->
                val byteArray = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(byteArray)
                return@map byteArray
            }.flatMap { tinifyService.compressImage(it) }.next()
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
