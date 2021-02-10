package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.dto.request.OnCreate
import org.springframework.http.codec.multipart.FilePart
import javax.validation.constraints.NotNull

class CreateImageRequest {
    @NotNull(groups = [OnCreate::class])
    var uuid: String? = null
    @NotNull(groups = [OnCreate::class])
    var title: String? = null
    @NotNull(groups = [OnCreate::class])
    var image: FilePart? = null
}