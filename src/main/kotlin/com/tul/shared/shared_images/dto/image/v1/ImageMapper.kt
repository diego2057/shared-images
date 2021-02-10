package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.model.Image
import org.mapstruct.Mapper
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

    fun updateModel(imageRequest: UpdateImageRequest, @MappingTarget image: Image)
}
