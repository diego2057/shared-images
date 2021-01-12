package com.tul.shared.shared_images.repository.gallery

import com.tul.shared.shared_images.model.Gallery
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository("gallery.crud_repository")
interface CrudRepository : ReactiveMongoRepository<Gallery, String>
