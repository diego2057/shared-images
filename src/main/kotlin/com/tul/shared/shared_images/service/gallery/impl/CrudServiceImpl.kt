package com.tul.shared.shared_images.service.gallery.impl

import com.fasterxml.jackson.databind.JsonNode
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.model.Gallery
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.gallery.CrudRepository
import com.tul.shared.shared_images.service.tinify.TinifyService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.service.gallery.CrudService as GalleryService

@Service("gallery.crud_service")
class CrudServiceImpl(
    private val galleryRepository: CrudRepository,
    private val tinifyService: TinifyService,
    private val imageMapper: ImageMapper,
    private val galleryMapper: GalleryMapper
) : GalleryService {

    override fun findAll(): Flux<Gallery> {
        return galleryRepository.findAll()
    }

    override fun findById(uuid: String): Mono<Gallery> {
        return galleryRepository.findById(uuid)
    }

    override fun save(galleryRequest: GalleryRequest): Mono<Gallery> {
        val gallery = galleryMapper.toModel(galleryRequest)
        return Flux.fromIterable(galleryRequest.images!!.asIterable())
            .flatMap { imageRequest ->
                val image = imageMapper.toModel(imageRequest)
                val file = imageRequest.image!!
                image.fileName = file.filename()
                image.mimeType = file.headers().getFirst("Content-Type")!!
                compressFilePartImage(image, imageRequest.image!!)
            }
            .collectList()
            .doOnNext { gallery.images = it }
            .thenReturn(gallery)
            .flatMap { galleryRepository.save(it) }
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
