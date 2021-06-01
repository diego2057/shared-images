package com.tul.shared.shared_images.controller.gallery.v1

import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
import com.tul.shared.shared_images.dto.gallery.v1.GalleryMapper
import com.tul.shared.shared_images.service.gallery.GalleryService
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.util.UUID

@RestController("_client.gallery.crud")
@RequestMapping("_client/v1/galleries")
@Api(tags = ["Client gallery ", "gallery"])
class ClientGalleryController(
    private val galleryService: GalleryService,
    private val galleryMapper: GalleryMapper
) {
    @GetMapping("/{id}")
    fun show(
        @PathVariable id: String,
        @RequestHeader(value = "security-uuid") securityUuid: UUID
    ): Mono<ResponseEntity<GalleryDto>> {
        return galleryService.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map { ResponseEntity.ok().body(galleryMapper.toDto(it)) }
    }
}
