package com.tul.shared.shared_images.controller.gallery.v1

import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest
import com.tul.shared.shared_images.dto.gallery.v1.UpdateGalleryRequest
import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.dto.request.OnCreateGallery
import com.tul.shared.shared_images.service.gallery.GalleryService
import io.swagger.annotations.Api
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

@RestController("gallery.crud")
@RequestMapping("v1/galleries")
@Api(tags = ["gallery", "crud"])
class GalleryController(
    private val galleryService: GalleryService,
    private val galleryMapper: GalleryMapper
) {
    @GetMapping
    fun index(): ResponseEntity<Flux<GalleryDto>> {
        return ResponseEntity.ok().body(galleryService.findAll().map { galleryMapper.toDto(it) })
    }

    @GetMapping("/{id}")
    fun show(@PathVariable id: String): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @PostMapping
    fun create(
        @Validated(OnCreateGallery::class)
        @ModelAttribute galleryRequest: GalleryRequest
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.save(galleryRequest)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @PatchMapping("/{id}/images")
    fun addImage(
        @Validated(OnCreateGallery::class)
        @ModelAttribute imageRequest: UpdateImageRequest,
        @PathVariable id: String
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.addImage(id, imageRequest)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @PostMapping("/{id}/images")
    fun addImagePost(
        @Validated(OnCreateGallery::class)
        @ModelAttribute imageRequest: UpdateImageRequest,
        @PathVariable id: String
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.addImage(id, imageRequest)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @PostMapping("/{id}/images/url")
    fun createUrl(
        @PathVariable id: String,
        @Validated(OnCreateGallery::class)
        @ModelAttribute imageRequest: ImageUrlRequest
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.addImage(id, imageRequest)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @PatchMapping("/{id}")
    fun update(
        @ModelAttribute gallery: UpdateGalleryRequest,
        @PathVariable id: String
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.update(id, gallery.images)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @DeleteMapping("/{id}/{imageId}")
    fun deleteImage(
        @PathVariable id: String,
        @PathVariable imageId: String
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.deleteImage(id, imageId)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @DeleteMapping("/{id}")
    fun deleteImages(
        @PathVariable id: String,
        @RequestBody imagesUuid: List<String>
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.deleteImages(id, imagesUuid)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }
}
