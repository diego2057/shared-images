package com.tul.shared.shared_images.dto.gallery.v1.kafka

import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest

class GalleryRequest(
    var uuid: String,
    var images: List<ImageRequest> = ArrayList()
)