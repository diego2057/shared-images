package com.tul.shared.shared_images.controller.image.v1

import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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
@AutoConfigureWebTestClient(timeout = "600000")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CrudControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun showImagesTest() {
        client.get()
            .uri("/v1/images")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("[]")
    }

    @Test
    fun createImageTest() {
        val imageRequest = ImageRequest()
        imageRequest.uuid = UUID.randomUUID().toString()
        imageRequest.title = "test"
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("request", imageRequest, MediaType.APPLICATION_JSON)

        client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(imageRequest.uuid!!)
            .jsonPath("file_name").isEqualTo("test.png")
    }

    @Test
    fun findImageByIdTest() {
        val imageRequest = ImageRequest()
        imageRequest.uuid = UUID.randomUUID().toString()
        imageRequest.title = "test"
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("request", imageRequest, MediaType.APPLICATION_JSON)

        val imageDto = client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        client.get()
            .uri("/v1/images/" + imageRequest.uuid)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(imageDto!!.uuid!!)
            .jsonPath("url").isEqualTo(imageDto.url!!)
    }

    @Test
    fun updateImageDataTest() {
        var imageRequest = ImageRequest()
        imageRequest.uuid = UUID.randomUUID().toString()
        imageRequest.title = "test"
        var bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("request", imageRequest, MediaType.APPLICATION_JSON)

        val imageDto = client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        imageRequest = ImageRequest()
        imageRequest.title = "test-modified"
        bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("request", imageRequest, MediaType.APPLICATION_JSON)

        client.patch()
            .uri("/v1/images/" + imageDto!!.uuid)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(imageDto!!.uuid!!)
            .jsonPath("title").isEqualTo(imageRequest.title!!)
    }

    @Test
    fun updateImageTest() {
        val imageRequest = ImageRequest()
        imageRequest.uuid = UUID.randomUUID().toString()
        imageRequest.title = "test"
        var bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("request", imageRequest, MediaType.APPLICATION_JSON)

        val createdImageDto = client.post()
            .uri("/v1/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ClassPathResource("test.jpg"), MediaType.MULTIPART_FORM_DATA)

        val updatedImageDto = client.patch()
            .uri("/v1/images/" + createdImageDto!!.uuid)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(ImageDto::class.java).returnResult().responseBody

        Assertions.assertEquals(updatedImageDto!!.title, createdImageDto.title)
        Assertions.assertNotEquals(updatedImageDto.url, createdImageDto.url)
    }
}
