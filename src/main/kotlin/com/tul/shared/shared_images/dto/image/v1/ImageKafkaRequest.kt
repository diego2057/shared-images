package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.dto.Action
import com.tul.shared.shared_images.dto.request.OnCreate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class ImageKafkaRequest {

    @NotBlank(groups = [OnCreate::class])
    var action: Action? = null

    @NotNull(groups = [OnCreate::class])
    var body: ImageUrlRequest? = null
}
