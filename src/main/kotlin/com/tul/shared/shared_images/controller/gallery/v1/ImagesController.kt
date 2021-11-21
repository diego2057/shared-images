package com.tul.shared.shared_images.controller.gallery.v1

import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
import com.tul.shared.shared_images.dto.gallery.v1.GalleryImagesRequest
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.dto.request.OnCreateGallery
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.repository.gallery.CrudRepository
import com.tul.shared.shared_images.service.gallery.GalleryService
import com.tul.shared.shared_images.service.image.ImageService
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController("gallery_images.crud")
@RequestMapping("v1/galleries")
@Api(tags = ["gallery", "crud"])
class ImagesController(
    private val galleryService: GalleryService,
    private val imageService: ImageService,
    private val galleryMapper: GalleryMapper,
    private val galleryRepository: CrudRepository,
    private val imageMapper: ImageMapper
) {

    @PostMapping("/{id}/images")
    fun create(
        @PathVariable id: String,
        @Validated(OnCreateGallery::class)
        @ModelAttribute galleryRequest: GalleryImagesRequest
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.save(id, galleryRequest)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @GetMapping("/{id}/images/{imageId}")
    fun showImageByGallery(@PathVariable id: String, @PathVariable imageId: String): Mono<ResponseEntity<ImageDto>> {
        return galleryService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map {
                var image: ImageDto? = null
                it.images.forEach {
                    if (it.uuid == imageId) image = imageMapper.toDto(it)
                }
                if (image != null) {
                    ResponseEntity.ok().body(image)
                } else {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND)
                }
            }
    }

    @PatchMapping("/{id}/images/{imageId}")
    fun updatedImagesByGallery(
        @Validated(OnCreateGallery::class)
        @ModelAttribute imageRequest: UpdateImageRequest,
        @PathVariable id: String,
        @PathVariable imageId: String
    ): Mono<ResponseEntity<ImageDto>> {
        return galleryService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map {
                var image: Image? = null
                it.images.forEach {
                    if (it.uuid == imageId) image = it
                }
                if (image != null) {
                    val newImage = imageService.update(imageRequest, image!!.uuid).block()
                    if (newImage != null) {
                        it.images.remove(image)
                        it.images.add(newImage)
                        galleryRepository.save(it)
                    }
                    ResponseEntity.ok().body(newImage?.let { it1 -> imageMapper.toDto(it1) })
                } else {
                    throw ResponseStatusException(HttpStatus.NOT_FOUND)
                }
            }
    }

    @DeleteMapping("/{id}/images/{imageId}")
    fun deleteImageByGallery(
        @PathVariable id: String,
        @PathVariable imageId: String
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.deleteImage(id, imageId)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }
}
