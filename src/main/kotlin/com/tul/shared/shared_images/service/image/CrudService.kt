package com.tul.shared.shared_images.service.image

import com.tul.shared.shared_images.model.Image
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CrudService {
    fun findAll(): Flux<Image>
    fun findById(id: String): Mono<Image>
    fun save(image: Image, imageFilePart: FilePart?): Mono<Image>
    fun delete(image: Image): Mono<Void>
}
