package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.dto.request.OnCreateGallery
import javax.validation.constraints.NotNull

class GalleryUrlRequest {

    @NotNull(groups = [OnCreateGallery::class])
    var uuid: String? = null

    @NotNull(groups = [OnCreateGallery::class])
    var images: List<ImageUrlRequest> = ArrayList()
}
