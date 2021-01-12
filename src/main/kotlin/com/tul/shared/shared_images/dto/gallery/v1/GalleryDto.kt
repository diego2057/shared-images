package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.model.Image
import reactor.core.publisher.Flux

class GalleryDto {
    var uuid: String? = null
    var images: Flux<Image> = Flux.empty()
}
