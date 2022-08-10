package com.tul.shared.shared_images.controller.gallery.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javafaker.Faker
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
import org.springframework.http.HttpMethod
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
class GalleryControllerTest {
    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private var tinifyMock = TinifyMock(8090)

    private val faker = Faker()

    @BeforeAll
    fun loadMock() {
        tinifyMock.startMockServer()
    }

    @AfterAll
    fun shutDownMock() {
        tinifyMock.stop()
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun showGalleryTest() {
        var galleryDtoArray = client.get()
            .uri("/v1/galleries")
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<GalleryDto>::class.java)
            .returnResult().responseBody!!

        Assertions.assertEquals(galleryDtoArray.size, 0)

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", UUID.randomUUID().toString())

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk

        galleryDtoArray = client.get()
            .uri("/v1/galleries")
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<GalleryDto>::class.java)
            .returnResult().responseBody!!

        Assertions.assertEquals(galleryDtoArray.size, 1)
    }

    @Test
    fun createGalleryTest() {
        val uuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
    }

    @Test
    fun findGalleryByIdTest() {
        val uuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk

        client.get()
            .uri("/v1/galleries/$uuid")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
    }

    @Test
    fun addImageTest() {
        val uuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        var bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)

        bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", file, MediaType.MULTIPART_FORM_DATA)

        client.patch()
            .uri("/v1/galleries/$uuid/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
            .jsonPath("images[1].uuid").isNotEmpty
            .jsonPath("images[1].file_name").isEqualTo(file.filename!!)
    }

    @Test
    fun addImagePostTest() {
        val uuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        var bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)

        bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", file, MediaType.MULTIPART_FORM_DATA)

        client.post()
            .uri("/v1/galleries/$uuid/create-images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
            .jsonPath("images[1].uuid").isNotEmpty
            .jsonPath("images[1].file_name").isEqualTo(file.filename!!)
    }

    @Test
    fun addImageNotExistGalleryTest() {
        val uuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", file, MediaType.MULTIPART_FORM_DATA)

        client.patch()
            .uri("/v1/galleries/$uuid/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
    }

    @Test
    fun createUrl() {
        val uuid = UUID.randomUUID().toString()

        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("fileName", file.filename!!)
        bodyBuilder.part("uuid", uuid)
        bodyBuilder.part("url", faker.internet().url())
        bodyBuilder.part("mimeType", "image/png")

        client.post()
            .uri("/v1/galleries/$uuid/images/url")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo("test.png")
    }

    @Test
    fun updateGalleryImagesTest() {
        val uuid = UUID.randomUUID().toString()
        var imageUUID = UUID.randomUUID().toString()
        var file = ClassPathResource("default.jpeg")

        var bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("image", file, MediaType.MULTIPART_FORM_DATA)

        val response = client.patch()
            .uri("/v1/galleries/$uuid/images")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java)
            .returnResult()
            .responseBody

        imageUUID = response?.images!![0].uuid!!

        file = ClassPathResource("test.png")
        bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("images[0].uuid", imageUUID)
        bodyBuilder.part("images[1].uuid", UUID.randomUUID().toString())
        bodyBuilder.part("images[2].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("images[2].uuid", UUID.randomUUID().toString())
        bodyBuilder.part("images[3].image", file, MediaType.MULTIPART_FORM_DATA)

        client.patch()
            .uri("/v1/galleries/$uuid")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isNotEmpty
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
            .jsonPath("images[1].uuid").isNotEmpty
            .jsonPath("images[1].file_name").isEqualTo(file.filename!!)
            .jsonPath("images[2].file_name").isEqualTo(file.filename!!)
            .jsonPath("images.length()").isEqualTo(3)
    }

    @Test
    fun deleteImageGalleryTest() {
        val uuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("images[1].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        var galleryDto = client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java).returnResult().responseBody

        Assertions.assertEquals(galleryDto?.images?.size, 2)

        val imageUuid = galleryDto?.images?.get(0)?.uuid

        galleryDto = client.delete()
            .uri("/v1/galleries/$uuid/$imageUuid")
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java).returnResult().responseBody

        Assertions.assertEquals(galleryDto?.images?.size, 1)
    }

    @Test
    fun deleteManyImagesGalleryTest() {
        val uuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("images[1].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid)

        var galleryDto = client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java).returnResult().responseBody

        Assertions.assertEquals(galleryDto?.images?.size, 2)

        val imageUuid1 = galleryDto?.images?.get(0)?.uuid
        val imageUuid2 = galleryDto?.images?.get(1)?.uuid

        galleryDto = client
            .method(HttpMethod.DELETE)
            .uri("/v1/galleries/$uuid")
            .bodyValue(listOf(imageUuid1, imageUuid2))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java).returnResult().responseBody

        Assertions.assertEquals(galleryDto?.images?.size, 0)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun multiple() {
        val uuid1 = UUID.randomUUID().toString()
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("uuid", uuid1)

        val uuid2 = UUID.randomUUID().toString()
        val bodyBuilder2 = MultipartBodyBuilder()
        bodyBuilder2.part("images[0].image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder2.part("uuid", uuid2)

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java).returnResult().responseBody

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder2.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(GalleryDto::class.java).returnResult().responseBody

        val galleryDtoArray = client.post()
            .uri("/v1/galleries/multiple")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(objectMapper.writeValueAsString(listOf(uuid1, uuid2))))
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<GalleryDto>::class.java)
            .returnResult().responseBody!!

        Assertions.assertEquals(2, galleryDtoArray.size)
    }
}
