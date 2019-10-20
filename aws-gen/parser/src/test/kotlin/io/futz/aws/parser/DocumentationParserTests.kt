package io.futz.aws.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentationParserTests {

    private val specVersion = "3.3.0"

    @Test
    fun `parse Lambda Function`() {
        val docs = DocumentationParser().parse(specVersion, "https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-lambda-function.html#cfn-lambda-function-memorysize")

        docs.forEach {
            println(it)
        }

        assertEquals(16, docs.size)
    }

    @Test
    fun `parse DynamoDb key schema`() {
        val docs = DocumentationParser().parse(specVersion, "https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-dynamodb-keyschema.html")
        docs.forEach {
            println(it)
        }
        assertEquals(2, docs.size)
    }

    @Test
    fun `parse EC2 Route Table`() {
        val docs = DocumentationParser().parse(specVersion, "http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-ec2-route-table.html")
        docs.forEach {
            println(it)
        }
        assertEquals(2, docs.size)
    }

    @Test
    fun `parse Route53 hosted zone`() {
        val docs = DocumentationParser().parse(specVersion, "http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-route53-hostedzone.html")
        docs.forEach {
            println(it)
        }
        assertEquals(5, docs.size)
    }

    @Test
    fun `parse EMR Instance Fleet Config`() {
        val docs = DocumentationParser().parse(specVersion, "https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-elasticmapreduce-instancefleetconfig.html")
        docs.forEach {
            println(it)
        }
        assertEquals(7, docs.size)
    }

    @Test
    fun `parse EC2 Trunk interface association - properties not found`() {
        val docs = DocumentationParser().parse(specVersion, "http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-ec2-trunkinterfaceassociation.html")
        docs.forEach {
            println(it)
        }
        assertEquals(0, docs.size)
    }
}