package com.tul.shared.shared_images.controller.global.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.controller.image.v1.ImageController
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.UUID

@SpringBootTest(classes = [TestConfiguration::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GlobalControllerTest {

    @Autowired
    private lateinit var imageController: ImageController

    @Autowired
    private lateinit var globalController: GlobalController

    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private var tinifyMock = TinifyMock(8090)

    @BeforeEach
    fun setup() {
        client = WebTestClient.bindToController(imageController, globalController).build()
    }

    @BeforeAll
    fun loadMock() {
        tinifyMock.startMockServer()
    }

    @AfterAll
    fun shutDownMock() {
        tinifyMock.stop()
    }

    @Test
    fun findImageByIdTest() {
        val uuid = UUID.randomUUID().toString()
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        val imageDto = client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        client.get()
            .uri("/_global/v1/backoffice/images/$uuid")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(imageDto!!.uuid!!)
            .jsonPath("url").isEqualTo(imageDto.url!!)
    }

    @Test
    fun notFoundImageByIdTest() {
        Thread.sleep(100)
        client.get()
            .uri("/_global/v1/backoffice/images/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun indexMultiple() {
        val uuid = UUID.randomUUID().toString()
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        val response = client.post()
            .uri("/_global/v1/backoffice/images/index/multiple")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(objectMapper.writeValueAsString(listOf(uuid))))
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<ImageDto>::class.java)
            .returnResult().responseBody!!

        Assertions.assertEquals(1, response.size)
    }
}
