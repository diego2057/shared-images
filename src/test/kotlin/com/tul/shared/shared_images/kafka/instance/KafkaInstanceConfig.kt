package com.tul.shared.shared_images.kafka.instance

import kafka.server.KafkaConfig
import org.springframework.context.annotation.Bean
import org.springframework.kafka.test.EmbeddedKafkaBroker

class KafkaInstanceConfig {

    @Bean
    fun embeddedKafkaBroker(): EmbeddedKafkaBroker? {
        val brokerProperties = mutableMapOf("listeners" to "PLAINTEXT://localhost:9093", "port" to "9093")
        return EmbeddedKafkaBroker(1, false, 1)
            .kafkaPorts(9093)
            .brokerProperty(KafkaConfig.AutoCreateTopicsEnableProp(), "true")
            .brokerProperties(brokerProperties)
    }
}
