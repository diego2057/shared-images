package com.tul.shared.shared_images.dto.image.v1

import org.springframework.http.codec.multipart.FilePart

class ImageRequest {
    var uuid: String? = null
    var title: String? = null
    var image: FilePart? = null

    fun getFileSize(): Int {
        return image.let { it!!.content().map { byteArray -> byteArray.readableByteCount() }.blockFirst()!! }
    }
}
