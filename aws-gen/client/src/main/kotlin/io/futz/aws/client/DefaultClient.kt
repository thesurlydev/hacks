package io.futz.aws.client

import io.futz.aws.model.Project
import io.futz.aws.model.StackOperation
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest
import software.amazon.awssdk.services.cloudformation.model.CreateStackResponse
import software.amazon.awssdk.services.cloudformation.model.DeleteStackRequest
import software.amazon.awssdk.services.cloudformation.model.EstimateTemplateCostRequest
import software.amazon.awssdk.services.cloudformation.model.UpdateStackRequest
import software.amazon.awssdk.services.cloudformation.model.ValidateTemplateRequest

class DefaultClient(val client: CloudFormationClient) : Client {

    override fun bootstrap() {
        TODO("not implemented")
    }

    override fun diff() {
        TODO("not implemented")
    }

    override fun deploy(stackName: String, template: String) {
        val validateTemplateResponse =
            client.validateTemplate(ValidateTemplateRequest.builder().templateBody(template).build())
        println(validateTemplateResponse.toString())
        println(client.estimateTemplateCost(EstimateTemplateCostRequest.builder().templateBody(template).build()).url())

        val createStackResponse: CreateStackResponse =
            client.createStack(
                CreateStackRequest.builder().templateBody(template).stackName(stackName).capabilities(
                    validateTemplateResponse.capabilities()
                ).build()
            )
        println(createStackResponse)
        waitForStack(client, StackOperation.CREATE, stackName)
    }

    override fun update(stackName: String, template: String) {

        val validateTemplateResponse =
            client.validateTemplate(ValidateTemplateRequest.builder().templateBody(template).build())

        val updateStackResponse = client.updateStack(
            UpdateStackRequest.builder()
                .capabilities(validateTemplateResponse.capabilities())
                .stackName(stackName)
                .templateBody(template)
                .build()
        )
        println(updateStackResponse)
        waitForStack(client, StackOperation.UPDATE, stackName)
    }

    override fun destroy(stackName: String) {
        val deleteStackResponse = client.deleteStack(DeleteStackRequest.builder().stackName(stackName).build())
        println(deleteStackResponse.toString())
        waitForStack(client, StackOperation.DELETE, stackName)
    }

    override fun ls(project: Project): List<String> {
        return project.stacks.keys.map { it }
    }
}