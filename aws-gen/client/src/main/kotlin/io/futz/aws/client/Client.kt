package io.futz.aws.client

import io.futz.aws.model.Project
import io.futz.aws.model.Stack
import io.futz.aws.model.StackOperation
import io.futz.aws.synthesizer.Synthesizer
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.CloudFormationException
import software.amazon.awssdk.services.cloudformation.model.DescribeStackEventsRequest
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest
import software.amazon.awssdk.services.cloudformation.model.StackSetNotFoundException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.TimeoutException

// TODO bootstrap
// TODO diff

interface Client {

    fun synthesize(stack: Stack): String = Synthesizer().synthesize(stack)

    fun bootstrap()

    fun diff()

    fun deploy(stackName: String, template: String)

    fun update(stackName: String, template: String)

    fun destroy(stackName: String)

    fun ls(project: Project): List<String>

    fun waitForStack(
        client: CloudFormationClient,
        stackOperation: StackOperation,
        stackId: String,
        timeout: Duration = Duration.ofMinutes(2L),
        leadWaitDuration: Duration = Duration.ofSeconds(5L),
        waitDuration: Duration = Duration.ofSeconds(1L)
    ) {
        println("Waiting for stack $stackOperation to complete")

        val now = Instant.now()
        val eventCache = mutableSetOf<String>()

        var firstIteration = true
        var completed: Boolean
        val start = LocalDateTime.now()
        do {
            if (firstIteration) {
                delayForDuration(leadWaitDuration)
            }

            val timedOut = Duration.between(start, LocalDateTime.now()) >= timeout
            if (timedOut) {
                throw TimeoutException("Timed out")
            }


            completed = try {

                client.describeStackEvents(DescribeStackEventsRequest.builder().stackName(stackId).build())
                    .let { response ->
                        response.stackEvents().asSequence()
                            .filter { event -> event.timestamp().isAfter(now) }
                            .sortedBy { event -> event.timestamp() }
                            .forEach { event ->
                                if (!eventCache.contains(event.eventId())) {
                                    eventCache.add(event.eventId())
                                    println(event.toString())
                                }
                            }
                    }

                val describeStacksResponse =
                    client.describeStacks(DescribeStacksRequest.builder().stackName(stackId).build())
                val status = describeStacksResponse.stacks()[0].stackStatusAsString()
                (stackOperation == StackOperation.CREATE && status == "CREATE_COMPLETE")
                        || (stackOperation == StackOperation.DELETE && status == "DELETE_COMPLETE")
                        || (stackOperation == StackOperation.UPDATE && status == "UPDATE_COMPLETE")
                        || status == "ROLLBACK_COMPLETE"
            } catch (cfe: CloudFormationException) {
                stackOperation == StackOperation.DELETE && cfe.message?.contains("does not exist")!!
            } catch (nf: StackSetNotFoundException) {
                if (!firstIteration) {
                    break
                } else {
                    System.err.println("Stack set not found")
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }

            if (!completed) {
                delayForDuration(waitDuration)
            }

            if (firstIteration) {
                firstIteration = false
            }

        } while (!timedOut && !completed)

        println("Done!")
    }

    private fun delayForDuration(duration: Duration) = try {
        Thread.sleep(duration.toMillis())
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}