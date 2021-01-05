package com.tul.shared.shared_images.dto.image.v1

class MessageImage {
    var byteArray: ByteArray? = null
    var fileName: String? = null
    var mimeType: String? = null
    var uuid: String? = null
    var title: String? = null

    fun getImageRequest(): ImageRequest {
        val imageRequest = ImageRequest()
        imageRequest.uuid = this.uuid
        imageRequest.title = this.title
        return imageRequest
    }
}
