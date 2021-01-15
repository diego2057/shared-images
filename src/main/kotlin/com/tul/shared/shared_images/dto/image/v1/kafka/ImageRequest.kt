package com.tul.shared.shared_images.dto.image.v1.kafka

class ImageRequest(
    var uuid: String,
    var byteArray: ByteArray? = null,
    var fileName: String? = null,
    var mimeType: String? = null,
    var title: String? = null
)
