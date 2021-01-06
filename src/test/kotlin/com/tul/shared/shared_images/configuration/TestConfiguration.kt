package com.tul.shared.shared_images.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.test.context.EmbeddedKafka

@TestConfiguration
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
class TestConfiguration {
    @Bean(destroyMethod = "shutdown")
    fun mongoServer(): MongoServer {
        val mongoServer = MongoServer(MemoryBackend())
        mongoServer.bind()
        return mongoServer
    }

    @Bean(destroyMethod = "close")
    fun mongoClient(mongoServer: MongoServer): MongoClient {
        return MongoClients.create("mongodb://" + mongoServer.localAddress.hostName + ":" + mongoServer.localAddress.port)
    }
}
