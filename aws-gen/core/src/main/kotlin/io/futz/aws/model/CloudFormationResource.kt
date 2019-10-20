package io.futz.aws.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class CloudFormationResource(
    @JsonIgnore val logicalId: String,
    val type: String,
    var properties: Map<String, Any?> = mapOf()
)