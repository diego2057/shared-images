package com.tul.shared.shared_images.kafka.instance

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.kafka.test.EmbeddedKafkaBroker

open class KafkaInstance {

    companion object {
        init {
            val context = AnnotationConfigApplicationContext(KafkaInstanceConfig::class.java)
            context.getBean(EmbeddedKafkaBroker::class.java)
            Runtime.getRuntime().addShutdownHook(Thread { context.close() })
        }
    }
}
