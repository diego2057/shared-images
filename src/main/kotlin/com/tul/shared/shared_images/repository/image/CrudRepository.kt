package com.tul.shared.shared_images.repository.image

import com.tul.shared.shared_images.model.Image
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface CrudRepository : ReactiveMongoRepository<Image, String>{
    fun findByUuid(uuid: String) : Mono<Image>
}
