package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.dto.Action
import com.tul.shared.shared_images.dto.request.OnCreateGallery
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class GalleryKafkaRequest {

    @NotBlank(groups = [OnCreateGallery::class])
    var action: Action? = null

    @NotNull(groups = [OnCreateGallery::class])
    var body: GalleryUrlRequest? = null
}
