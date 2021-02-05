package com.tul.shared.shared_images.config.kafka.image

import com.fasterxml.jackson.databind.ObjectMapper
import com.tul.shared.shared_images.config.kafka.KafkaConfig
import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class ImageKafka(private val objectMapper: ObjectMapper) {

    @Bean(name = ["config.kafka.image.consumerFactory"])
    fun consumerFactory(kafkaProperties: KafkaProperties): ConcurrentKafkaListenerContainerFactory<String, ImageRequest> {
        val containerFactory: ConcurrentKafkaListenerContainerFactory<String, ImageRequest> = ConcurrentKafkaListenerContainerFactory<String, ImageRequest>()
        val consumerFactory: DefaultKafkaConsumerFactory<String, ImageRequest> = DefaultKafkaConsumerFactory(
            KafkaConfig.getConfig(kafkaProperties),
            StringDeserializer(),
            JsonDeserializer(ImageRequest::class.java, objectMapper)
        )
        containerFactory.consumerFactory = consumerFactory
        return containerFactory
    }
}
