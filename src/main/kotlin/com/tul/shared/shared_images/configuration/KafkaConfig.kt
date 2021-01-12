package com.tul.shared.shared_images.configuration

import com.tul.shared.shared_images.dto.image.v1.ImageRequest
import com.tul.shared.shared_images.dto.image.v1.MessageImage
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfig(
        @Value("\${spring.kafka.consumer.bootstrap-servers}")
        private val bootstrapServer: String,
        @Value("\${spring.kafka.consumer.group-id}")
        private val groupId: String,
){
    @Bean
    fun consumerConfiguration(): Map<String?, Any?> {
        return mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.GROUP_ID_CONFIG to groupId
        )
    }

    @Bean
    fun messageImageKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, MessageImage> {
        val containerFactory: ConcurrentKafkaListenerContainerFactory<String, MessageImage> = ConcurrentKafkaListenerContainerFactory<String, MessageImage>()
        val consumerFactory: DefaultKafkaConsumerFactory<String, MessageImage> = DefaultKafkaConsumerFactory(consumerConfiguration(), StringDeserializer(), JsonDeserializer(MessageImage::class.java))
        containerFactory.consumerFactory = consumerFactory
        return containerFactory
    }

}
