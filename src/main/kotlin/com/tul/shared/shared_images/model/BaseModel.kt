package com.tul.shared.shared_images.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

abstract class BaseModel() {
    @Version
    var version: Long? = null

    @Field(name = "created_at")
    @CreatedDate
    var createdAt: LocalDateTime? = null

    @Field(name = "last_modified_at")
    @LastModifiedDate
    var lastModifiedAt: LocalDateTime? = null
}
