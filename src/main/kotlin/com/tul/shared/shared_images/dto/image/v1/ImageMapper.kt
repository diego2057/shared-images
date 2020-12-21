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
    fun toDtoFromRequest(imageRequest: ImageRequest): ImageDto

    @Mappings(
        Mapping(target = "fileName", expression = "java(imageRequest.getImage().filename())"),
        Mapping(target = "mimeType", expression = "java(imageRequest.getImage().headers().getFirst(\"Content-Type\"))")
    )
    fun toModel(imageRequest: ImageRequest): Image

    fun updateModel(imageDto: ImageDto, @MappingTarget image: Image)
}
