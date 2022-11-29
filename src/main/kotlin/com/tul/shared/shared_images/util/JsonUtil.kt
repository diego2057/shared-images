package com.tul.shared.shared_images.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonUtil {

    @Autowired
    fun setUp(objectMapper: ObjectMapper) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        Companion.objectMapper = objectMapper
    }

    companion object {
        private lateinit var objectMapper: ObjectMapper

        fun Any.asJsonString(): String {
            return objectMapper.writeValueAsString(this)
        }
    }
}
