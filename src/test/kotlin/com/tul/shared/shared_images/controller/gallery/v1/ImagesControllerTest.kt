package com.tul.shared.shared_images.controller.gallery.v1

import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
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

@SpringBootTest(classes = [TestConfiguration::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImagesControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

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
    fun create() {
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        val id = UUID.randomUUID()

        client.post()
            .uri("/v1/galleries/$id/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun showImageByGalleryNotFound() {
        val id = UUID.randomUUID()
        val imageId = UUID.randomUUID()
        client.get()
            .uri("/v1/galleries/$id/images/$imageId")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun showImageByGallery() {

        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        val id = UUID.randomUUID()

        val response = client.post()
            .uri("/v1/galleries/$id/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java)
            .returnResult().responseBody!!

        val imageId = response.images?.get(0)?.uuid
        client.get()
            .uri("/v1/galleries/$id/images/$imageId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(imageId!!)
            .jsonPath("file_name").isEqualTo(file.filename!!)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun showImageByGalleryNotFoundImageId() {

        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        val id = UUID.randomUUID()

        client.post()
            .uri("/v1/galleries/$id/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk

        val imageId = UUID.randomUUID()
        client.get()
            .uri("/v1/galleries/$id/images/$imageId")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun deleteImageGalleryTest() {
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        val id = UUID.randomUUID()

        val response = client.post()
            .uri("/v1/galleries/$id/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java)
            .returnResult().responseBody!!

        val imageUuid = response.images?.get(0)?.uuid

        val galleryDto = client.delete()
            .uri("/v1/galleries/$id/images/$imageUuid")
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java).returnResult().responseBody

        Assertions.assertEquals(galleryDto?.images?.size, 0)
    }
}
