package com.tul.shared.shared_images.repository.gallery

import com.tul.shared.shared_images.model.Gallery
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository("gallery.crud_repository")
interface CrudRepository : ReactiveMongoRepository<Gallery, String> {
    fun findByUuidIn(listUuids: List<String>): Flux<Gallery>
}
