package com.tul.shared.shared_images.service.image.impl

import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.image.CrudRepository
import com.tul.shared.shared_images.service.image.CrudService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CrudServiceImpl(
    private val imageCrudRepository: CrudRepository
) : CrudService {
    override fun findAll(): Flux<Image> {
        return imageCrudRepository.findAll()
    }

    override fun findById(id: String): Mono<Image> {
        return imageCrudRepository.findById(id)
    }

    override fun save(image: Image): Mono<Image> {
        return imageCrudRepository.save(image)
    }

    override fun delete(image: Image): Mono<Void> {
        return imageCrudRepository.delete(image)
    }
}
