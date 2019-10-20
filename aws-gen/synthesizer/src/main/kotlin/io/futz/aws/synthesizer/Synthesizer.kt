package io.futz.aws.synthesizer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule

class Synthesizer {

    fun synthesize(any: Any): String {
        return mapper().writeValueAsString(any)
    }

    private fun mapper(): ObjectMapper = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .registerModule(KotlinModule())
}