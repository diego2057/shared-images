package com.tul.shared.shared_images.kafka.images

import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.dto.Action
import com.tul.shared.shared_images.dto.image.v1.ImageKafkaRequest
import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.kafka.images.v1.ImagesConsumer
import com.tul.shared.shared_images.service.image.ImageService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@SpringBootTest(classes = [TestConfiguration::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9093", "port=9093"])
class ImagesConsumerTest {

    @Autowired
    private lateinit var service: ImageService

    @Autowired
    private lateinit var imagesConsumer: ImagesConsumer

    private var tinifyMock = TinifyMock(8090)

    @BeforeAll
    fun loadMock() {
        tinifyMock.startMockServer()
    }

    @AfterAll
    fun shutDownMock() {
        tinifyMock.stop()
    }

    @Test
    fun created() {
        val imageUUID = UUID.randomUUID().toString()

        val imageKafkaRequest = ImageKafkaRequest().apply {
            action = Action.create
            body = ImageUrlRequest().apply {
                uuid = imageUUID
                fileName = "test.png"
                url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
            }
        }

        imagesConsumer.images(imageKafkaRequest)
        val image = service.findById(imageUUID).block()

        assertNotNull(image)
        assertEquals(imageUUID, image?.uuid)
        assertEquals("test.png", image?.fileName)
        assertNotNull(image?.url)
    }

    @Test
    fun update() {
        val imageUUID = UUID.randomUUID().toString()

        val imageKafkaRequest = ImageKafkaRequest().apply {
            action = Action.update
            body = ImageUrlRequest().apply {
                uuid = imageUUID
                fileName = "test.png"
                url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
            }
        }

        imagesConsumer.images(imageKafkaRequest)
        val image = service.findById(imageUUID).block()

        assertNotNull(image)
        assertEquals(imageUUID, image?.uuid)
        assertEquals("test.png", image?.fileName)
        assertNotNull(image?.url)
    }

    @Test
    fun noAction() {
        val imageUUID = UUID.randomUUID().toString()

        val imageKafkaRequest = ImageKafkaRequest().apply {
            body = ImageUrlRequest().apply {
                uuid = imageUUID
                fileName = "test.png"
                url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
            }
        }

        imagesConsumer.images(imageKafkaRequest)
        val image = service.findById(imageUUID).block()

        assertNotNull(image)
        assertEquals("00000000-0000-0000-0000-000000000000", image?.uuid)
    }

    @Test
    fun noBodyCreate() {
        val imageUUID = UUID.randomUUID().toString()

        val imageKafkaRequest = ImageKafkaRequest().apply {
            action = Action.create
        }

        imagesConsumer.images(imageKafkaRequest)
        val image = service.findById(imageUUID).block()

        assertNotNull(image)
        assertEquals("00000000-0000-0000-0000-000000000000", image?.uuid)
    }

    @Test
    fun noBodyUpdate() {
        val imageUUID = UUID.randomUUID().toString()

        val imageKafkaRequest = ImageKafkaRequest().apply {
            action = Action.update
        }

        imagesConsumer.images(imageKafkaRequest)
        val image = service.findById(imageUUID).block()

        assertNotNull(image)
        assertEquals("00000000-0000-0000-0000-000000000000", image?.uuid)
    }
}
