package com.tul.shared.shared_images.dto.image.v1

import org.springframework.http.codec.multipart.FilePart

class UpdateImageRequest {
    var title: String? = null
    var image: FilePart? = null
}
