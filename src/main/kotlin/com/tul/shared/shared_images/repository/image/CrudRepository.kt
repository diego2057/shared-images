package com.tul.shared.shared_images.repository.image

import com.tul.shared.shared_images.model.Image
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository("image.crud_repository")
interface CrudRepository : ReactiveMongoRepository<Image, String> {
    fun findByUuidIn(listUuids: List<String>): Flux<Image>
}
