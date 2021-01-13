package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.dto.image.v1.ImageRequest
import com.tul.shared.shared_images.dto.request.OnCreate
import com.tul.shared.shared_images.dto.request.OnUpdate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

class GalleryRequest {
    @NotNull(groups = [OnCreate::class])
    @Null(groups = [OnUpdate::class])
    var uuid: String? = null
    @NotNull(groups = [OnCreate::class])
    var images: List<ImageRequest>? = null
}
