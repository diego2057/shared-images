package com.tul.shared.shared_images.controller.global.v1

import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.model.Image
import com.tul.shared.shared_images.service.image.ImageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController("global.v1")
@RequestMapping("_global/v1/backoffice")
class GlobalController(
    private val imageService: ImageService,
    private val imageMapper: ImageMapper,
) {

    @GetMapping("/images/{id}")
    fun show(@PathVariable id: String): Mono<ResponseEntity<ImageDto>> =
        imageService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }

    @PostMapping("/images/index/multiple")
    fun indexMultiple(@RequestBody listIds: List<String>): Flux<Image> {
        return imageService.findIndexMultiple(listIds)
    }
}
