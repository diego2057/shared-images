package com.tul.shared.shared_images.service.image.impl

import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.image.CrudRepository
import com.tul.shared.shared_images.service.image.CrudService
import com.tul.shared.shared_images.service.tinify.TinifyClient
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CrudServiceImpl(
    private val imageCrudRepository: CrudRepository,
    private val tinifyClient: TinifyClient
) : CrudService {

    fun tinyWebService(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.tinify.com")
            .build()
    }

    override fun findAll(): Flux<Image> {
        return imageCrudRepository.findAll()
    }

    override fun findById(id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
    }

    override fun save(image: Image, imageFilePart: FilePart?): Mono<Image> {
        if(imageFilePart != null){
            image.fileName = imageFilePart.filename()
            image.mimeType = imageFilePart.headers().getFirst("Content-Type")!!
            return imageFilePart.content()
                    .map { dataBuffer ->
                        val byteArray = ByteArray(dataBuffer.readableByteCount())
                        dataBuffer.read(byteArray)
                        return@map byteArray
                    }.flatMap { tinifyClient.compressImage(it) }
                    .doOnNext { image.size = it.get("input").get("size").asLong() }
                    .flatMap { tinifyClient.storeImage(it.get("output").get("url").textValue(), image.fileName!!) }
                    .flatMap {
                        image.url = it
                        imageCrudRepository.save(image)
                    }
                    .next()
        }
        else{
            return imageCrudRepository.save(image)
        }
    }


    override fun delete(image: Image): Mono<Void> {
        return imageCrudRepository.delete(image)
    }
}
