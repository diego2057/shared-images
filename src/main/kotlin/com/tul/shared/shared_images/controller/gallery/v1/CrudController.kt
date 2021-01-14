package com.tul.shared.shared_images.controller.gallery.v1

import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.dto.gallery.v1.rest.GalleryRequest
import com.tul.shared.shared_images.dto.request.OnCreate
import com.tul.shared.shared_images.service.gallery.CrudService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController("gallery.crud")
@RequestMapping("v1/galleries")
class CrudController(
    private val galleryCrudService: CrudService,
    private val galleryMapper: GalleryMapper
) {
    @GetMapping
    fun index(): ResponseEntity<Flux<GalleryDto>> {
        return ResponseEntity.ok().body(galleryCrudService.findAll().map { galleryMapper.toDto(it) })
    }

    @GetMapping("/{id}")
    fun show(@PathVariable id: String): Mono<ResponseEntity<GalleryDto>> {
        return galleryCrudService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }

    @PostMapping
    fun create(
        @Validated(OnCreate::class)
        @ModelAttribute galleryRequest: GalleryRequest
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryCrudService.save(galleryRequest)
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }
}
