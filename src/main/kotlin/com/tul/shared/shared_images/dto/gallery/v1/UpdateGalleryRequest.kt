package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.dto.image.v1.CreateImageRequest

class UpdateGalleryRequest {
    val images: List<CreateImageRequest> = mutableListOf()
}
