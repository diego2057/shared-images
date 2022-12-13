package com.tul.shared.shared_images.kafka.galleries

import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.dto.Action
import com.tul.shared.shared_images.dto.gallery.v1.GalleryKafkaRequest
import com.tul.shared.shared_images.dto.gallery.v1.GalleryUrlRequest
import com.tul.shared.shared_images.dto.image.v1.ImageUrlRequest
import com.tul.shared.shared_images.kafka.galleries.v1.GalleriesConsumer
import com.tul.shared.shared_images.service.gallery.GalleryService
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
class GalleriesConsumerTest {

    @Autowired
    private lateinit var service: GalleryService

    @Autowired
    private lateinit var galleriesConsumer: GalleriesConsumer

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
        val galleryUUID = UUID.randomUUID().toString()
        val imageUUID1 = UUID.randomUUID().toString()
        val imageUUID2 = UUID.randomUUID().toString()

        val image1 = ImageUrlRequest().apply {
            uuid = imageUUID1
            fileName = "test.png"
            url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
        }

        val image2 = ImageUrlRequest().apply {
            uuid = imageUUID2
            fileName = "test.png"
            url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
        }

        val galleryKafkaRequest = GalleryKafkaRequest().apply {
            action = Action.create
            body = GalleryUrlRequest().apply {
                uuid = galleryUUID
                images = listOf(image1, image2)
            }
        }

        galleriesConsumer.galleries(galleryKafkaRequest)
        val gallery = service.findById(galleryUUID).block()

        assertNotNull(gallery)
        assertEquals(galleryUUID, gallery?.uuid)
        assertEquals(2, gallery?.images?.size)
    }

    @Test
    fun update() {
        val galleryUUID = UUID.randomUUID().toString()
        val imageUUID1 = UUID.randomUUID().toString()
        val imageUUID2 = UUID.randomUUID().toString()

        val image1 = ImageUrlRequest().apply {
            uuid = imageUUID1
            fileName = "test.png"
            url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
        }

        val image2 = ImageUrlRequest().apply {
            uuid = imageUUID2
            fileName = "test.png"
            url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
        }

        val galleryKafkaRequest = GalleryKafkaRequest().apply {
            action = Action.update
            body = GalleryUrlRequest().apply {
                uuid = galleryUUID
                images = listOf(image1, image2)
            }
        }

        galleriesConsumer.galleries(galleryKafkaRequest)
        val gallery = service.findById(galleryUUID).block()

        assertNotNull(gallery)
        assertEquals(galleryUUID, gallery?.uuid)
        assertEquals(2, gallery?.images?.size)
    }

    @Test
    fun noAction() {
        val galleryUUID = UUID.randomUUID().toString()

        val galleryKafkaRequest = GalleryKafkaRequest().apply {
            body = GalleryUrlRequest().apply {
                uuid = galleryUUID
                images = listOf(
                    ImageUrlRequest().apply {
                        uuid = UUID.randomUUID().toString()
                        fileName = "test.png"
                        url = "https://backoffice.tul.com.co/assets/images/logo-tul.png"
                    }
                )
            }
        }

        galleriesConsumer.galleries(galleryKafkaRequest)
        val gallery = service.findById(galleryUUID).block()

        assertNotNull(gallery)
        assertEquals(galleryUUID, gallery?.uuid)
        assertEquals(0, gallery?.images?.size)
    }

    @Test
    fun noBodyCreate() {
        val galleryUUID = UUID.randomUUID().toString()

        val galleryKafkaRequest = GalleryKafkaRequest().apply {
            action = Action.create
        }

        galleriesConsumer.galleries(galleryKafkaRequest)
        val gallery = service.findById(galleryUUID).block()

        assertNotNull(gallery)
        assertEquals(galleryUUID, gallery?.uuid)
        assertEquals(0, gallery?.images?.size)
    }

    @Test
    fun noBodyCreateId() {
        val galleryUUID = UUID.randomUUID().toString()

        val galleryKafkaRequest = GalleryKafkaRequest().apply {
            action = Action.create
            body = GalleryUrlRequest()
        }

        galleriesConsumer.galleries(galleryKafkaRequest)
        val gallery = service.findById(galleryUUID).block()

        assertNotNull(gallery)
        assertEquals(galleryUUID, gallery?.uuid)
        assertEquals(0, gallery?.images?.size)
    }

    @Test
    fun noBodyUpdateId() {
        val galleryUUID = UUID.randomUUID().toString()

        val galleryKafkaRequest = GalleryKafkaRequest().apply {
            action = Action.update
            body = GalleryUrlRequest()
        }

        galleriesConsumer.galleries(galleryKafkaRequest)
        val gallery = service.findById(galleryUUID).block()

        assertNotNull(gallery)
        assertEquals(galleryUUID, gallery?.uuid)
        assertEquals(0, gallery?.images?.size)
    }

    @Test
    fun noBodyUpdate() {
        val galleryUUID = UUID.randomUUID().toString()

        val galleryKafkaRequest = GalleryKafkaRequest().apply {
            action = Action.update
        }

        galleriesConsumer.galleries(galleryKafkaRequest)
        val gallery = service.findById(galleryUUID).block()

        assertNotNull(gallery)
        assertEquals(galleryUUID, gallery?.uuid)
        assertEquals(0, gallery?.images?.size)
    }
}
