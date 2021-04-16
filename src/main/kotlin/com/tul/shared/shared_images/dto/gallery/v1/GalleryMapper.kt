package com.tul.shared.shared_images.dto.gallery.v1

import com.tul.shared.shared_images.model.Gallery
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy
import com.tul.shared.shared_images.dto.gallery.v1.GalleryRequest as RestGalleryRequest

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
)
abstract class GalleryMapper {
    abstract fun toDto(gallery: Gallery): GalleryDto

    @Mappings(
        Mapping(target = "images", expression = "java(new java.util.ArrayList())")
    )
    abstract fun toModel(galleryRequest: RestGalleryRequest): Gallery
}
