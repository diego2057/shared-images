package com.tul.shared.shared_images.configuration

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfiguration {

    /*@Bean
    fun mongoTemplate(mongoDbFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(mongoDbFactory)
    }*/

    /*@Bean
    fun mongoDbFactory(mongoServer: MongoServer): MongoDatabaseFactory {
        val serverAddress = mongoServer.localAddress
        return SimpleMongoClientDatabaseFactory("mongodb://" + serverAddress.hostName + ":" + serverAddress.port + "/test")
    }*/

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
