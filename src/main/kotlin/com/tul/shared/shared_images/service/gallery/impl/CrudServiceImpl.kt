package com.tul.shared.shared_images.service.gallery.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.KafkaProducerTopic
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.gallery.GalleryProducer
import com.tul.shared.shared_images.model.Gallery
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.gallery.CrudRepository
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.gallery.v1.kafka.GalleryRequest as KafkaGalleryRequest
import com.tul.shared.shared_images.dto.gallery.v1.rest.GalleryRequest as RestGalleryRequest
import com.tul.shared.shared_images.service.gallery.CrudService as GalleryService

@Service("gallery.crud_service")
class CrudServiceImpl(
    private val galleryRepository: CrudRepository,
    private val tinifyService: TinifyService,
    private val imageMapper: ImageMapper,
    private val galleryMapper: GalleryMapper,
    private val galleryProducer: GalleryProducer
) : GalleryService {

    override fun findAll(): Flux<Gallery> {
        return galleryRepository.findAll()
    }

    override fun findById(uuid: String): Mono<Gallery> {
        return galleryRepository.findById(uuid)
    }

    override fun save(galleryRequest: RestGalleryRequest): Mono<Gallery> {
        val gallery = galleryMapper.toModel(galleryRequest)
        return Flux.fromIterable(galleryRequest.images.asIterable())
            .flatMap { imageRequest ->
                val image = imageMapper.toModel(imageRequest)
                val file = imageRequest.image!!
                image.fileName = file.filename()
                image.mimeType = file.headers().getFirst("Content-Type")
                compressFilePartImage(image, file)
            }
            .collectList()
            .doOnNext { gallery.images = it }
            .thenReturn(gallery)
            .flatMap { galleryRepository.save(it) }
            .doOnNext { galleryProducer.sendMessage(it, KafkaProducerTopic.CREATED_GALLERY) }
    }

    override fun save(galleryRequest: KafkaGalleryRequest): Mono<Gallery> {
        val gallery = galleryMapper.toModelFromMessage(galleryRequest)
        return Flux.fromIterable(galleryRequest.images.asIterable())
            .flatMap { imageRequest ->
                val image = imageMapper.toModelFromMessage(imageRequest)
                tinifyService.compressImage(imageRequest.byteArray!!).flatMap { storeImage(image, it) }
            }
            .collectList()
            .doOnNext { gallery.images = it }
            .thenReturn(gallery)
            .flatMap { galleryRepository.save(it) }
            .doOnNext { galleryProducer.sendMessage(it, KafkaProducerTopic.CREATED_GALLERY) }
    }

    private fun compressFilePartImage(image: Image, imageFilePart: FilePart): Mono<Image> {
        return imageFilePart.content()
            .map { dataBuffer ->
                val byteArray = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(byteArray)
                return@map byteArray
            }.flatMap { tinifyService.compressImage(it) }.next()
            .flatMap { storeImage(image, it) }
    }

    private fun storeImage(image: Image, jsonNode: JsonNode): Mono<Image> {
        image.size = jsonNode.get("input").get("size").asLong()
        return tinifyService.storeImage(jsonNode.get("output").get("url").textValue(), image.fileName!!)
            .doOnNext { image.url = it }
            .thenReturn(image)
    }
}