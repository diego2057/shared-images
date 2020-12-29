package com.tul.shared.shared_images.repository.image

import com.tul.shared.shared_images.model.Image
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface CrudRepository : ReactiveMongoRepository<Image, String>
