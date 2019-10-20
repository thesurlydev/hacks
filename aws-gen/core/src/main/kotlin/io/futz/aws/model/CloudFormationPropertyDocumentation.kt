package io.futz.aws.model

data class CloudFormationPropertyDocumentation(val name: String,
                                               val description: String? = null,
                                               val required: String? = null,
                                               val type: String? = null,
                                               val pattern: String? = null,
                                               val minimum: Int?,
                                               val maximum: Int?,
                                               val allowedValues: Set<String>? = emptySet(),
                                               val updateRequires: String? = null,
                                               val typeLink: String? = null,
                                               val link: String? = null) {

}