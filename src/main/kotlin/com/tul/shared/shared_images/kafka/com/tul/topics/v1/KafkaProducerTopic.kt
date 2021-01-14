package com.tul.shared.shared_images.kafka.com.tul.topics.v1

enum class KafkaProducerTopic(val topic: String) {
    CREATED_IMAGE("api.tul.shared.shared_images.v1.images.created"),
    UPDATED_IMAGE("api.tul.shared.shared_images.v1.images.updated"),
    CREATED_GALLERY("api.tul.shared.shared_images.v1.gallery.created")
}
