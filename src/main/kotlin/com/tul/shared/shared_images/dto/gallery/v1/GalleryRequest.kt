package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.dto.image.v1.ImageRequest
import com.tul.shared.shared_images.dto.request.OnCreate
import javax.validation.constraints.NotNull

class GalleryRequest(
    @NotNull(groups = [OnCreate::class])
    var uuid: String,
    @NotNull(groups = [OnCreate::class])
    var images: List<ImageRequest> = ArrayList()
)
