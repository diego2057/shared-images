package com.tul.shared.shared_images.dto.image.v1

import com.tul.shared.shared_images.model.Image
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import org.mapstruct.Mappings
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy
import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest as KafkaImageRequest
import com.tul.shared.shared_images.dto.image.v1.rest.ImageRequest as RestImageRequest

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface ImageMapper {
    @Mappings
    fun toDto(image: Image): ImageDto

    @Mappings
    fun toDtoFromRequest(imageRequest: RestImageRequest): ImageDto

    @Mappings
    fun toModel(imageRequest: RestImageRequest): Image

    @Mappings
    fun toModelFromMessage(imageRequest: KafkaImageRequest): Image

    fun updateModel(imageDto: ImageDto, @MappingTarget image: Image)
}
