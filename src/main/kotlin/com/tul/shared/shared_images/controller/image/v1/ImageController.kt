package com.tul.shared.shared_images.controller.image.v1

import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.dto.request.OnCreate
import com.tul.shared.shared_images.dto.request.OnUpdate
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.service.image.ImageService
import io.swagger.annotations.Api
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController("image.crud")
@RequestMapping("v1/images")
@Api(tags = ["Image", "crud"])
class ImageController(
    private val imageService: ImageService,
    private val imageMapper: ImageMapper,
) {
    companion object {
        const val cacheName = "v1/images/show"
    }
    @GetMapping
    fun index(): ResponseEntity<Flux<ImageDto>> {
        return ResponseEntity.ok().body(imageService.findAll().map { imageMapper.toDto(it) })
    }

    @GetMapping("/{id}")
    @Cacheable(cacheName, key = "#id")
    fun show(@PathVariable id: String): Mono<ResponseEntity<ImageDto>> {
        return imageService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @PostMapping
    fun create(
        @Validated(OnCreate::class) @ModelAttribute createImageRequest: CreateImageRequest
    ): Mono<ResponseEntity<ImageDto>> {
        return imageService.save(createImageRequest)
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @PostMapping("/url")
    fun createFromUrl(
        @Validated(OnCreate::class) @RequestBody imageUrlRequest: ImageUrlRequest
    ): Mono<ResponseEntity<ImageDto>> {
        return imageService.saveOrUpdateFromUrl(imageUrlRequest)
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @PatchMapping("/{id}")
    @Caching(
        evict = [
            CacheEvict(cacheName, key = "#id")
        ],
        put = [
            CachePut(cacheName, key = "#id")
        ]
    )
    fun update(
        @PathVariable id: String,
        @Validated(OnUpdate::class) @ModelAttribute updateImageRequest: UpdateImageRequest
    ): Mono<ResponseEntity<ImageDto>> {
        return imageService.update(updateImageRequest, id)
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @DeleteMapping("/{id}")
    @Caching(
        evict = [
            CacheEvict(cacheName, key = "#id", beforeInvocation = true)
        ]
    )
    fun delete(@PathVariable id: String): Mono<ResponseEntity<Void>> {
        return imageService.delete(id)
            .thenReturn(ResponseEntity.noContent().build())
    }

    @PostMapping("/index/multiple")
    fun indexMultiple(@RequestBody listIds: List<String>): Flux<Image> {
        return imageService.findIndexMultiple(listIds)
    }
}
