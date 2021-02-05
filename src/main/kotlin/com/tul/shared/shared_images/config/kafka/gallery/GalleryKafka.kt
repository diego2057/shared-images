package com.tul.shared.shared_images.config.kafka.gallery

import com.fasterxml.jackson.databind.ObjectMapper
import com.tul.shared.shared_images.config.kafka.KafkaConfig
import com.tul.shared.shared_images.dto.gallery.v1.kafka.GalleryRequest
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class GalleryKafka(private val objectMapper: ObjectMapper) {

    @Bean(name = ["config.kafka.gallery.consumerFactory"])
    fun consumerFactory(kafkaProperties: KafkaProperties): ConcurrentKafkaListenerContainerFactory<String, GalleryRequest> {
        val containerFactory: ConcurrentKafkaListenerContainerFactory<String, GalleryRequest> = ConcurrentKafkaListenerContainerFactory<String, GalleryRequest>()
        val consumerFactory: DefaultKafkaConsumerFactory<String, GalleryRequest> = DefaultKafkaConsumerFactory(
            KafkaConfig.getConfig(kafkaProperties),
            StringDeserializer(),
            JsonDeserializer(GalleryRequest::class.java, objectMapper)
        )
        containerFactory.consumerFactory = consumerFactory
        return containerFactory
    }
}
