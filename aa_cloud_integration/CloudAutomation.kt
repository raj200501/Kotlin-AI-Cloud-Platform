package aa_cloud_integration

import com.google.cloud.compute.v1.*
import com.google.cloud.scheduler.v1.CloudSchedulerClient
import com.google.cloud.scheduler.v1.Job
import com.google.cloud.scheduler.v1.JobName
import com.google.cloud.scheduler.v1.PubsubTarget
import com.google.protobuf.Duration
import com.google.protobuf.util.Timestamps

object CloudAutomation {

    private val projectId = "your-gcp-project-id"
    private val zone = "us-central1-a"

    fun createScheduledTask(jobName: String, schedule: String, topicName: String, message: String) {
        CloudSchedulerClient.create().use { cloudSchedulerClient ->
            val job = Job.newBuilder()
                .setName(JobName.of(projectId, "us-central1", jobName).toString())
                .setSchedule(schedule)
                .setTimeZone("UTC")
                .setPubsubTarget(
                    PubsubTarget.newBuilder()
                        .setTopicName(topicName)
                        .setData(message.toByteArray())
                        .build()
                )
                .build()

            cloudSchedulerClient.createJob("projects/$projectId/locations/us-central1", job)
            println("Scheduled task $jobName created.")
        }
    }

    fun listScheduledTasks() {
        CloudSchedulerClient.create().use { cloudSchedulerClient ->
            val parent = "projects/$projectId/locations/us-central1"
            val response = cloudSchedulerClient.listJobs(parent)
            response.iterateAll().forEach { job ->
                println("Job Name: ${job.name}")
                println("Schedule: ${job.schedule}")
                println("Time Zone: ${job.timeZone}")
                println("Pubsub Target: ${job.pubsubTarget}")
                println()
            }
        }
    }

    fun deleteScheduledTask(jobName: String) {
        CloudSchedulerClient.create().use { cloudSchedulerClient ->
            val jobNameFormatted = JobName.of(projectId, "us-central1", jobName).toString()
            cloudSchedulerClient.deleteJob(jobNameFormatted)
            println("Scheduled task $jobName deleted.")
        }
    }

    fun automateInstanceStartStop(instanceName: String, startSchedule: String, stopSchedule: String) {
        val topicName = "projects/$projectId/topics/instance-control"
        createScheduledTask(
            jobName = "$instanceName-start",
            schedule = startSchedule,
            topicName = topicName,
            message = "START $instanceName"
        )
        createScheduledTask(
            jobName = "$instanceName-stop",
            schedule = stopSchedule,
            topicName = topicName,
            message = "STOP $instanceName"
        )
    }

    fun startInstance(instanceName: String) {
        val startInstanceRequest = StartInstanceRequest.newBuilder()
            .setProject(projectId)
            .setZone(zone)
            .setInstance(instanceName)
            .build()

        InstancesClient.create().use { computeClient ->
            val operation = computeClient.start(startInstanceRequest)
            waitForOperationCompletion(operation.name)
        }
    }

    fun stopInstance(instanceName: String) {
        val stopInstanceRequest = StopInstanceRequest.newBuilder()
            .setProject(projectId)
            .setZone(zone)
            .setInstance(instanceName)
            .build()

        InstancesClient.create().use { computeClient ->
            val operation = computeClient.stop(stopInstanceRequest)
            waitForOperationCompletion(operation.name)
        }
    }

    private fun waitForOperationCompletion(operationName: String) {
        var operation: Operation
        InstancesClient.create().use { computeClient ->
            do {
                operation = computeClient.get(projectId, zone, operationName)
                if (operation.status == Operation.Status.DONE) {
                    if (operation.error != null) {
                        throw RuntimeException("Error during operation: ${operation.error.errorsList}")
                    }
                    println("Operation completed successfully.")
                    return
                }
                println("Waiting for operation to complete...")
                Thread.sleep(1000)
            } while (operation.status != Operation.Status.DONE)
        }
    }

    fun createPubSubTopic(topicName: String) {
        val topic = Topic.newBuilder().setName("projects/$projectId/topics/$topicName").build()
        TopicAdminClient.create().use { topicAdminClient ->
            topicAdminClient.createTopic(topic)
            println("Pub/Sub topic $topicName created.")
        }
    }

    fun deletePubSubTopic(topicName: String) {
        TopicAdminClient.create().use { topicAdminClient ->
            topicAdminClient.deleteTopic("projects/$projectId/topics/$topicName")
            println("Pub/Sub topic $topicName deleted.")
        }
    }

    fun publishMessage(topicName: String, message: String) {
        Publisher.newBuilder("projects/$projectId/topics/$topicName").build().use { publisher ->
            val pubsubMessage = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(message))
                .build()

            val messageIdFuture = publisher.publish(pubsubMessage)
            val messageId = messageIdFuture.get()
            println("Message published with ID: $messageId")
        }
    }

    fun subscribeToTopic(subscriptionName: String, topicName: String) {
        val subscription = Subscription.newBuilder()
            .setName("projects/$projectId/subscriptions/$subscriptionName")
            .setTopic("projects/$projectId/topics/$topicName")
            .build()
        SubscriptionAdminClient.create().use { subscriptionAdminClient ->
            subscriptionAdminClient.createSubscription(subscription)
            println("Subscription $subscriptionName created for topic $topicName.")
        }
    }

    fun pullMessages(subscriptionName: String) {
        Subscriber.newBuilder("projects/$projectId/subscriptions/$subscriptionName") { message, consumer ->
            println("Message received: ${message.data.toStringUtf8()}")
            consumer.ack()
        }.build().startAsync().awaitRunning()
    }
}
