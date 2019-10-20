package io.futz.aws.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(
    "awsTemplateFormatVersion",
    "description",
    "metadata",
    "resources",
    "outputs"
)
data class Stack(
    @JsonIgnore val project: Project,
    @JsonIgnore val stackName: String,
    @JsonIgnore val region: String,
    @JsonProperty(value = "AWSTemplateFormatVersion") val awsTemplateFormatVersion: String = "2010-09-09",
    val description: String? = null,
    val metadata: String? = null,
    val resources: MutableMap<String, CloudFormationResource> = mutableMapOf(),
    val outputs: MutableMap<String, Output> = mutableMapOf()
)