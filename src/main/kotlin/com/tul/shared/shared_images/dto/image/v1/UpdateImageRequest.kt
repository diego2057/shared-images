package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.dto.request.OnCreateGallery
import org.springframework.http.codec.multipart.FilePart
import javax.validation.constraints.NotNull

class UpdateImageRequest {
    @NotNull(groups = [OnCreateGallery::class])
    var title: String? = null
    @NotNull(groups = [OnCreateGallery::class])
    var image: FilePart? = null
}
