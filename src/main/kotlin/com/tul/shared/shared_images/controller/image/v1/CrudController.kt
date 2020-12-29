package com.tul.shared.shared_images.controller.image.v1

import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.ImageRequest
import com.tul.shared.shared_images.dto.request.OnCreate
import com.tul.shared.shared_images.dto.request.OnUpdate
import com.tul.shared.shared_images.service.image.CrudService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("v1/images")
class CrudController(
    private val imageCrudService: CrudService,
    private val imageMapper: ImageMapper
) {
    @GetMapping
    fun index(): ResponseEntity<Flux<ImageDto>> {
        return ResponseEntity.ok().body(imageCrudService.findAll().map { imageMapper.toDto(it) })
    }

    @GetMapping("/{id}")
    fun show(@PathVariable id: String): Mono<ResponseEntity<ImageDto>> {
        return imageCrudService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @PostMapping
    fun create(
        @RequestPart("file") filePart: FilePart,
        @Validated(OnCreate::class) @RequestPart("request") imageRequest: ImageRequest
    ): Mono<ResponseEntity<ImageDto>> {
        return imageCrudService.save(imageMapper.toModel(imageRequest), filePart)
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @RequestPart("file", required = false) filePart: FilePart?,
        @Validated(OnUpdate::class) @RequestPart("request", required = false) imageRequest: ImageRequest?

    ): Mono<ResponseEntity<ImageDto>> {
        return imageCrudService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .doOnNext { if (imageRequest != null) imageMapper.updateModel(imageMapper.toDtoFromRequest(imageRequest), it) }
            .flatMap { imageCrudService.save(it, filePart) }
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Mono<ResponseEntity<Void>> {
        return imageCrudService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .flatMap { imageCrudService.delete(it) }
            .thenReturn(ResponseEntity.noContent().build())
    }
}
