package com.tul.shared.shared_images.controller.image.v1

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageRequest
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
    fun showImagesTest() {
        val imageDtoArray = getAllImages()
        Assertions.assertEquals(imageDtoArray.size, 0)
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
            .uri("/v1/images/${imageRequest.uuid}")
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
            .uri("/v1/images/${imageDto!!.uuid}")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(imageDto.uuid!!)
            .jsonPath("title").isEqualTo(imageRequest.title!!)
    }

    @Test
    fun deleteImageTest() {
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

        var imageDtoArray = getAllImages()

        Assertions.assertEquals(imageDtoArray.size, 1)

        client.delete()
            .uri("/v1/images/${imageDtoArray[0].uuid}")
            .exchange()
            .expectStatus().isNoContent

        imageDtoArray = getAllImages()
        Assertions.assertEquals(imageDtoArray.size, 0)
    }

    private fun getAllImages(): Array<ImageDto> {
        return client.get()
            .uri("/v1/images")
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<ImageDto>::class.java)
            .returnResult().responseBody!!
    }
}
