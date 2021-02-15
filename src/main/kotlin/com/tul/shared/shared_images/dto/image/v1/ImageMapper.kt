package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.model.Image
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Mappings
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface ImageMapper {
    @Mappings
    fun toDto(image: Image): ImageDto

    @Mappings
    fun toDtoFromRequest(imageRequest: CreateImageRequest): ImageDto

    @Mappings
    fun toModel(imageRequest: CreateImageRequest): Image

    @Mappings(
        Mapping(target = "uuid", expression = "java(java.util.UUID.randomUUID().toString())")
    )
    fun toModel(imageRequest: UpdateImageRequest): Image

    fun updateModel(imageRequest: UpdateImageRequest, @MappingTarget image: Image)
}
