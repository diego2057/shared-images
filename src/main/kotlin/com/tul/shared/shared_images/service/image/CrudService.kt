package com.tul.shared.shared_images.service.image

import com.tul.shared.shared_images.model.Image
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest as KafkaImageRequest
import com.tul.shared.shared_images.dto.image.v1.rest.ImageRequest as RestImageRequest

interface CrudService {
    fun findAll(): Flux<Image>
    fun findById(id: String): Mono<Image>
    fun save(imageRequest: RestImageRequest): Mono<Image>
    fun save(messageImage: KafkaImageRequest): Mono<Image>
    fun update(imageRequest: RestImageRequest, id: String): Mono<Image>
    fun update(messageImage: KafkaImageRequest): Mono<Image>
    fun delete(id: String): Mono<Void>
}
