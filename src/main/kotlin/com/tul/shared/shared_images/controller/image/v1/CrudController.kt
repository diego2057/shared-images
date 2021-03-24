package com.tul.shared.shared_images.controller.image.v1

import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.dto.request.OnCreate
import com.tul.shared.shared_images.dto.request.OnUpdate
import com.tul.shared.shared_images.service.image.CrudService
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
        @Validated(OnCreate::class) @ModelAttribute createImageRequest: CreateImageRequest
    ): Mono<ResponseEntity<ImageDto>> {
        return imageCrudService.save(createImageRequest)
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Validated(OnUpdate::class) @ModelAttribute updateImageRequest: UpdateImageRequest

    ): Mono<ResponseEntity<ImageDto>> {
        return imageCrudService.update(updateImageRequest, id)
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Mono<ResponseEntity<Void>> {
        return imageCrudService.delete(id)
            .thenReturn(ResponseEntity.noContent().build())
    }

    @PostMapping("/index/multiple")
    fun indexMultiple(@RequestBody listIds: List<String>): ResponseEntity<Flux<ImageDto>> {
        return ResponseEntity.ok().body(imageCrudService.findIndexMultiple(listIds).map { imageMapper.toDto(it) })
    }
}
