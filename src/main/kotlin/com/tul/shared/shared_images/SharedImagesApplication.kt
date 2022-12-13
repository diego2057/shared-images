package com.tul.shared.shared_images

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.tul.*"])
class SharedImagesApplication

fun main(args: Array<String>) {
    runApplication<SharedImagesApplication>(*args)
}
