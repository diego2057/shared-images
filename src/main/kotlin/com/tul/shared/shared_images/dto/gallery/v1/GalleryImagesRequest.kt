package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.dto.image.v1.UpdateImageRequest
import com.tul.shared.shared_images.dto.request.OnCreateGallery
import javax.validation.constraints.NotNull

class GalleryImagesRequest(
    @NotNull(groups = [OnCreateGallery::class])
    var images: List<UpdateImageRequest> = ArrayList()
)
