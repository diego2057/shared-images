package com.tul.shared.shared_images.kafka.com.tul.topics.v1.image

enum class KafkaImageProducerTopic(val topic: String) {
    CREATED("api.tul.shared.shared_images.v1.images.created"),
    UPDATED("api.tul.shared.shared_images.v1.images.updated");
}
