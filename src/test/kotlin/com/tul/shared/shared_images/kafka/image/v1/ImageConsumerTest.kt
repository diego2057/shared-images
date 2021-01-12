package com.tul.shared.shared_images.kafka.image.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.dto.image.v1.MessageImage
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var imageConsumer: ImageConsumer

    private lateinit var wireMockServer: WireMockServer

    @BeforeAll
    fun loadMock() {
        wireMockServer = WireMockServer(8090)
        wireMockServer.start()

        wireMockServer.stubFor(
            WireMock.post("/shrink")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(ClassPathResource("mock/tinify-api-shrink-response.json").file.readText())
                )
        )

        wireMockServer.stubFor(
            WireMock.post("/output/Th1s1s4t35t")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withHeader(HttpHeaders.LOCATION, "https://s3.us-east-2.amazonaws.com/images/test.png")
                        .withBody("{ status : success }")
                )
        )
    }

    @AfterAll
    fun shutDownMock() {
        wireMockServer.stop()
    }

    @Test
    fun kafkaCreateImage() {
        val file = ClassPathResource("test.png")

        val messageImage = MessageImage()
        messageImage.uuid = UUID.randomUUID().toString()
        messageImage.title = "test"
        messageImage.fileName = file.filename
        messageImage.mimeType = MediaType.IMAGE_PNG_VALUE
        messageImage.byteArray = file.file.readBytes()

        imageConsumer.create(objectMapper.writeValueAsString(messageImage))

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
        var messageImage = MessageImage()
        messageImage.uuid = uuid
        messageImage.title = "test"
        messageImage.fileName = file.filename
        messageImage.mimeType = MediaType.IMAGE_PNG_VALUE
        messageImage.byteArray = file.file.readBytes()

        imageConsumer.create(objectMapper.writeValueAsString(messageImage))

        Thread.sleep(1000)

        messageImage = MessageImage()
        messageImage.title = "test-updated"
        messageImage.uuid = uuid

        imageConsumer.update(objectMapper.writeValueAsString(messageImage))

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
