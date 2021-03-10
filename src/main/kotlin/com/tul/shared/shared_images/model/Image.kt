package com.tul.shared.shared_images.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import javax.validation.constraints.NotNull

@Document
data class Image(
    @Id
    @NotNull
    var uuid: String,

    @Field("title")
    @NotNull
    var title: String,

    @Field("file_name")
    @NotNull
    var fileName: String?,

    @Field("mime_type")
    @NotNull
    var mimeType: String?,

    @Field("size")
    @NotNull
    var size: Long?,

    @Field("url")
    @NotNull
    var url: String?,
)
