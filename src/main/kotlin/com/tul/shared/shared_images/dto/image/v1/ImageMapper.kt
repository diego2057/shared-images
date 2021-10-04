package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.model.Image
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Mappings
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy
import java.util.*

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
abstract class ImageMapper {
    @Mappings
    abstract fun toDto(image: Image): ImageDto

    @Mappings(
        Mapping(target = "uuid", defaultExpression = "java(java.util.UUID.randomUUID().toString())")
    )
    abstract fun toModel(imageRequest: CreateImageRequest): Image

    @Mappings(
        Mapping(target = "uuid", defaultExpression = "java(java.util.UUID.randomUUID().toString())")
    )
    abstract fun toModel(imageRequest: UpdateImageRequest): Image

    fun toModel(imageUrlRequest: ImageUrlRequest): Image {
        val uuid = imageUrlRequest.uuid!!
        val extensionIndex = imageUrlRequest.fileName?.lastIndexOf('.')
        val mimeType = "image/${ imageUrlRequest.fileName?.substring(extensionIndex?.plus(1) ?: 0)}"
        val url = imageUrlRequest.url
        return Image(uuid, imageUrlRequest.fileName, mimeType, null, url)
    }

    fun updateModel(imageUrlRequest: ImageUrlRequest, image: Image) {
        imageUrlRequest.fileName?.let {
            image.fileName = it
            image.mimeType = "image/${it.substring(it.lastIndexOf('.') + 1)}"
        }
        imageUrlRequest.url?.let { image.url = it }
    }

    abstract fun updateModel(imageRequest: UpdateImageRequest, @MappingTarget image: Image)

    abstract fun updateModel(imageRequest: CreateImageRequest, @MappingTarget image: Image)
}
