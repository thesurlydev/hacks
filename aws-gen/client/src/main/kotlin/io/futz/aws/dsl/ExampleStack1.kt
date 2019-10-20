package io.futz.aws.dsl

import aws.dynamodb.dynamodbTable
import aws.dynamodb.table.AttributeDefinition
import aws.dynamodb.table.KeySchema
import aws.dynamodb.table.ProvisionedThroughput
import io.futz.aws.model.Project
import io.futz.aws.model.stack

class ExampleStack1(project: Project) {

    init {
        project.stack("example1", "us-east-1") {
            dynamodbTable("example1", listOf(KeySchema("id", "HASH"))) {
                tableName = "example1TableName"
                attributeDefinitions = listOf(AttributeDefinition("id", "S"))
                provisionedThroughput = ProvisionedThroughput(1, 1)
            }
        }
    }
}