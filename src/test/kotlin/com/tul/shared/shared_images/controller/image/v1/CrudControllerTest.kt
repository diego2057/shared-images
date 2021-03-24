package com.tul.shared.shared_images.controller.image.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrudControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
    fun showImagesTest() {
        Thread.sleep(100)
        val initialSize = getAllImages().size
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", UUID.randomUUID().toString())
        bodyBuilder.part("title", "test")

        client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()

        val newSize = getAllImages().size
        Assertions.assertEquals(initialSize + 1, newSize)
    }

    @Test
    fun createImageTest() {
        val uuid = UUID.randomUUID().toString()
        val title = "test"
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)
        bodyBuilder.part("title", title)

        client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("file_name").isEqualTo("test.png")

        client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun findImageByIdTest() {
        val uuid = UUID.randomUUID().toString()
        val title = "test"
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)
        bodyBuilder.part("title", title)

        val imageDto = client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        client.get()
            .uri("/v1/images/$uuid")
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
            .uri("/v1/images/${UUID.randomUUID()}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("title").isEqualTo("default")
            .jsonPath("url").isEqualTo("https://s3.us-east-2.amazonaws.com/images/default.jpeg")
    }

    @Test
    fun updateImageDataTest() {
        val uuid = UUID.randomUUID().toString()
        var title = "test"
        var bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)
        bodyBuilder.part("title", title)

        val imageDto = client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        title = "test-modified"
        bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("title", title)

        client.patch()
            .uri("/v1/images/${imageDto!!.uuid}")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(imageDto.uuid!!)
            .jsonPath("title").isEqualTo(title)
    }

    @Test
    fun deleteImageTest() {
        val uuid = UUID.randomUUID().toString()
        val title = "test"
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)
        bodyBuilder.part("title", title)

        client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk

        val imageDtoArray = getAllImages()

        client.delete()
            .uri("/v1/images/${imageDtoArray[0].uuid}")
            .exchange()
            .expectStatus().isNoContent

        val newSize = getAllImages().size
        Assertions.assertEquals(imageDtoArray.size - 1, newSize)
    }

    private fun getAllImages(): Array<ImageDto> {
        return client.get()
            .uri("/v1/images")
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<ImageDto>::class.java)
            .returnResult().responseBody!!
    }

    @Test
    fun indexMultiple() {
        val uuid = UUID.randomUUID().toString()
        val title = "test"
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)
        bodyBuilder.part("title", title)

        val imageDto = client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        val response = client.post()
            .uri("/v1/images/index/multiple")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(objectMapper.writeValueAsString(listOf(uuid))))
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<ImageDto>::class.java)
            .returnResult().responseBody!!

        Assertions.assertEquals(1, response.size)
    }
}
