package io.futz.aws.dsl

import io.futz.aws.client.DefaultClient
import io.futz.aws.model.project
import software.amazon.awssdk.services.cloudformation.CloudFormationClient

fun main() {

    val p = project("test1", "1231123") {

        ExampleStack1(this)

        /*stack("appmesh", "us-west-2") {
            appmeshMesh("mesh1", "mesh1") {
                spec = MeshSpec
            }
        }

        stack("foo", "us-east-1") {

            lambdaFunction("lambda1", Code(), "handler", "role", "runtime") {
                memorySize = 128
            }

            dynamodbTable("bar", listOf(KeySchema("id", "HASH"))) {
                tableName = "tableName"
                attributeDefinitions = listOf(AttributeDefinition("id", "S"))
                provisionedThroughput = ProvisionedThroughput(1, 1)
            }

            iamRole(
                "iam", """
            {
               "Version" : "2012-10-17",
               "Statement": [ {
                  "Effect": "Allow",
                  "Principal": {
                     "Service": [ "ec2.amazonaws.com" ]
                  },
                  "Action": [ "sts:AssumeRole" ]
               } ]
            }
            """.trimIndent()
            ) {
                roleName = "shanerole2"
            }
        }*/
    }


    val cloudFormationClient = CloudFormationClient.create()
    val client = DefaultClient(cloudFormationClient)

    p.stacks.forEach {

        println()
        println(it.key)
        repeat(it.key.length) { print("-") }
        println()

        val template = client.synthesize(it.value)
        println(template)
    }


    // TODO generate CloudFormationResource Props data classes to replace properties on ResourceTypes?
    // TODO inspect
    // TODO cross-stack sharing


//    client.deploy(stack.stackName, template)
}

