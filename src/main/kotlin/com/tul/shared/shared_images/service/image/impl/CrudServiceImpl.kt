package com.tul.shared.shared_images.service.image.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.image.ImageProducer
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.image.KafkaImageProducerTopic
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
    private val tinifyService: TinifyService,
    private val imageProducer: ImageProducer
) : CrudService {

    override fun findAll(): Flux<Image> {
        return imageCrudRepository.findAll()
    }

    override fun findById(id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
    }

    override fun save(image: Image, imageFilePart: FilePart): Mono<Image> {
        image.fileName = imageFilePart.filename()
        image.mimeType = imageFilePart.headers().getFirst("Content-Type")!!
        return compressFilePartImage(imageFilePart)
            .flatMap { storeImage(image, it) }
            .doOnNext { imageProducer.sendMessage(it, KafkaImageProducerTopic.CREATED) }
    }

    override fun save(image: Image, byteArray: ByteArray): Mono<Image> {
        return tinifyService.compressImage(byteArray)
            .flatMap { storeImage(image, it) }
            .doOnNext { imageProducer.sendMessage(it, KafkaImageProducerTopic.CREATED) }
    }

    override fun update(image: Image, imageFilePart: FilePart?): Mono<Image> {
        val updatedResult = if (imageFilePart != null) {
            image.fileName = imageFilePart.filename()
            image.mimeType = imageFilePart.headers().getFirst("Content-Type")!!
            compressFilePartImage(imageFilePart).flatMap { storeImage(image, it) }
        } else {
            imageCrudRepository.save(image)
        }
        return updatedResult.doOnNext { imageProducer.sendMessage(it, KafkaImageProducerTopic.CREATED) }
    }

    override fun update(image: Image, byteArray: ByteArray?): Mono<Image> {
        val updatedResult = if (byteArray != null) {
            tinifyService.compressImage(byteArray).flatMap { storeImage(image, it) }
        } else {
            imageCrudRepository.save(image)
        }
        return updatedResult.doOnNext { imageProducer.sendMessage(it, KafkaImageProducerTopic.CREATED) }
    }

    override fun delete(image: Image): Mono<Void> {
        return imageCrudRepository.delete(image)
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
