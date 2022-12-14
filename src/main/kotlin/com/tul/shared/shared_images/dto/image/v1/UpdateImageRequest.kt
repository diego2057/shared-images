package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.dto.request.OnCreateGallery
import org.springframework.web.multipart.MultipartFile
import javax.validation.constraints.NotNull

class UpdateImageRequest {
    var uuid: String? = null
    @NotNull(groups = [OnCreateGallery::class])
    var image: MultipartFile? = null
}
