package com.tul.shared.shared_images.controller.image.v1

import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageMapper
import com.tul.shared.shared_images.service.image.ImageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController("_client.image.crud")
@RequestMapping("_client/v1/images")
class ClientImageController(
    private val imageService: ImageService,
    private val imageMapper: ImageMapper
) {
    @GetMapping("/{id}")
    fun show(@PathVariable id: String): Mono<ResponseEntity<ImageDto>> {
        return imageService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map { ResponseEntity.ok().body(imageMapper.toDto(it)) }
    }
}
