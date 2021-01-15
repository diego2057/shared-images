package com.tul.shared.shared_images.kafka.gallery.v1

import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.dto.gallery.v1.kafka.GalleryRequest
import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.gallery.GalleryConsumer
import com.tul.shared.shared_images.service.gallery.CrudService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@SpringBootTest(classes = [TestConfiguration::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GalleryConsumerTest {
    @Autowired
    private lateinit var galleryCrudService: CrudService

    @Autowired
    private lateinit var galleryConsumer: GalleryConsumer

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
    fun kafkaCreateImage() {

        val file = ClassPathResource("test.png")

        val imageRequest = ImageRequest(UUID.randomUUID().toString()).apply {
            title = "test"
            fileName = file.filename
            mimeType = MediaType.IMAGE_PNG_VALUE
            byteArray = file.file.readBytes()
        }

        val galleryRequest = GalleryRequest(UUID.randomUUID().toString()).apply {
            uuid = UUID.randomUUID().toString()
            images = listOf(imageRequest)
        }

        galleryConsumer.create(galleryRequest)

        Thread.sleep(1000)

        val gallery = galleryRequest.uuid.let { galleryCrudService.findById(it).block() }
        if (gallery != null) {
            Assertions.assertEquals(1, gallery.images?.size)
            Assertions.assertEquals(imageRequest.fileName, gallery.images?.get(0)?.fileName)
            Assertions.assertEquals(imageRequest.title, gallery.images?.get(0)?.title)
        } else {
            Assertions.fail()
        }
    }
}
