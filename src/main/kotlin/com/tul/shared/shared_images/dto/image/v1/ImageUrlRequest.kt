package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.dto.request.OnCreate
import javax.validation.constraints.NotNull

class ImageUrlRequest {
    @NotNull(groups = [OnCreate::class])
    var uuid: String? = null
    @NotNull(groups = [OnCreate::class])
    var fileName: String? = null
    @NotNull(groups = [OnCreate::class])
    var url: String? = null
    var mimeType: String? = null
}
