package com.tul.shared.shared_images.kafka.image.v1

import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.dto.image.v1.kafka.ImageRequest
import com.tul.shared.shared_images.kafka.com.tul.topics.v1.image.ImageConsumer
import com.tul.shared.shared_images.service.image.CrudService
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
class ImageConsumerTest {
    @Autowired
    private lateinit var imageCrudService: CrudService

    @Autowired
    private lateinit var imageConsumer: ImageConsumer

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

        val messageImage = ImageRequest()
        messageImage.uuid = UUID.randomUUID().toString()
        messageImage.title = "test"
        messageImage.fileName = file.filename
        messageImage.mimeType = MediaType.IMAGE_PNG_VALUE
        messageImage.byteArray = file.file.readBytes()

        imageConsumer.create(messageImage)

        Thread.sleep(1000)

        val image = imageCrudService.findById(messageImage.uuid).block()
        if (image != null) {
            Assertions.assertEquals(image.fileName, messageImage.fileName)
            Assertions.assertEquals(image.title, messageImage.title)
        } else {
            Assertions.fail()
        }
    }

    @Test
    fun kafkaUpdateImage() {
        val file = ClassPathResource("test.png")
        val uuid = UUID.randomUUID().toString()
        var messageImage = ImageRequest()
        messageImage.uuid = uuid
        messageImage.title = "test"
        messageImage.fileName = file.filename
        messageImage.mimeType = MediaType.IMAGE_PNG_VALUE
        messageImage.byteArray = file.file.readBytes()

        imageConsumer.create(messageImage)

        Thread.sleep(1000)

        messageImage = ImageRequest()
        messageImage.title = "test-updated"
        messageImage.uuid = uuid

        imageConsumer.update(messageImage)

        Thread.sleep(1000)

        val image = imageCrudService.findById(messageImage.uuid).block()

        if (image != null) {
            Assertions.assertEquals(image.fileName, file.filename)
            Assertions.assertEquals(image.title, messageImage.title)
        } else {
            Assertions.fail()
        }
    }
}
