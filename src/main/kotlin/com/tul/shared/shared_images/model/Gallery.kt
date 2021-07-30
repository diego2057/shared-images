package com.tul.shared.shared_images.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import javax.validation.constraints.NotNull

@Document
data class Gallery(
    @Id
    @NotNull
    var uuid: String,

    @Field("images")
    @NotNull
    var images: MutableList<Image> = mutableListOf()
) : BaseModel()
