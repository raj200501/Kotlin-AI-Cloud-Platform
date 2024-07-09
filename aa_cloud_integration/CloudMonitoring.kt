package aa_cloud_integration

import com.google.cloud.monitoring.v3.MetricServiceClient
import com.google.cloud.monitoring.v3.ProjectName
import com.google.monitoring.v3.*
import com.google.protobuf.util.Timestamps
import java.time.Instant
import java.time.temporal.ChronoUnit

object CloudMonitoring {

    private val projectId = "your-gcp-project-id"

    fun listMonitoredResourceDescriptors() {
        MetricServiceClient.create().use { metricServiceClient ->
            val projectName = ProjectName.of(projectId)
            val response = metricServiceClient.listMonitoredResourceDescriptors(projectName)
            response.iterateAll().forEach { descriptor ->
                println(descriptor.type)
                println(descriptor.displayName)
                println(descriptor.description)
                println(descriptor.labelsList)
                println()
            }
        }
    }

    fun createCustomMetricDescriptor() {
        MetricServiceClient.create().use { metricServiceClient ->
            val projectName = ProjectName.of(projectId)
            val metricDescriptor = MetricDescriptor.newBuilder()
                .setType("custom.googleapis.com/my_custom_metric")
                .setDescription("This is a custom metric.")
                .setDisplayName("My Custom Metric")
                .setMetricKind(MetricDescriptor.MetricKind.GAUGE)
                .setValueType(MetricDescriptor.ValueType.DOUBLE)
                .setUnit("1")
                .addLabels(LabelDescriptor.newBuilder()
                    .setKey("environment")
                    .setDescription("The environment where this metric is recorded.")
                    .setValueType(LabelDescriptor.ValueType.STRING)
                    .build())
                .build()

            val request = CreateMetricDescriptorRequest.newBuilder()
                .setName(projectName.toString())
                .setMetricDescriptor(metricDescriptor)
                .build()

            metricServiceClient.createMetricDescriptor(request)
            println("Custom metric descriptor created.")
        }
    }

    fun writeCustomMetricValue() {
        MetricServiceClient.create().use { metricServiceClient ->
            val projectName = ProjectName.of(projectId)
            val metric = Metric.newBuilder()
                .setType("custom.googleapis.com/my_custom_metric")
                .putLabels("environment", "production")
                .build()

            val resource = MonitoredResource.newBuilder()
                .setType("global")
                .putLabels("project_id", projectId)
                .build()

            val point = Point.newBuilder()
                .setInterval(
                    TimeInterval.newBuilder()
                        .setEndTime(Timestamps.fromMillis(Instant.now().toEpochMilli()))
                        .build()
                )
                .setValue(TypedValue.newBuilder().setDoubleValue(123.45).build())
                .build()

            val timeSeries = TimeSeries.newBuilder()
                .setMetric(metric)
                .setResource(resource)
                .addPoints(point)
                .build()

            metricServiceClient.createTimeSeries(projectName, listOf(timeSeries))
            println("Custom metric value written.")
        }
    }

    fun listTimeSeries() {
        MetricServiceClient.create().use { metricServiceClient ->
            val projectName = ProjectName.of(projectId)
            val filter = "metric.type = \"compute.googleapis.com/instance/cpu/usage_time\""
            val interval = TimeInterval.newBuilder()
                .setEndTime(Timestamps.fromMillis(Instant.now().toEpochMilli()))
                .setStartTime(Timestamps.fromMillis(Instant.now().minus(1, ChronoUnit.HOURS).toEpochMilli()))
                .build()

            val request = ListTimeSeriesRequest.newBuilder()
                .setName(projectName.toString())
                .setFilter(filter)
                .setInterval(interval)
                .setView(ListTimeSeriesRequest.TimeSeriesView.FULL)
                .build()

            val response = metricServiceClient.listTimeSeries(request)
            response.iterateAll().forEach { timeSeries ->
                println("Metric type: ${timeSeries.metric.type}")
                println("Resource type: ${timeSeries.resource.type}")
                timeSeries.pointsList.forEach { point ->
                    println("Value: ${point.value}")
                    println("Timestamp: ${point.interval.endTime}")
                }
                println()
            }
        }
    }

    fun createAlertPolicy() {
        AlertPolicyServiceClient.create().use { alertPolicyServiceClient ->
            val projectName = ProjectName.of(projectId)
            val condition = AlertPolicy.Condition.newBuilder()
                .setDisplayName("CPU Usage High")
                .setConditionThreshold(
                    AlertPolicy.Condition.MetricThreshold.newBuilder()
                        .setFilter("metric.type=\"compute.googleapis.com/instance/cpu/usage_time\"")
                        .setComparison(ComparisonType.COMPARISON_GT)
                        .setThresholdValue(0.8)
                        .setDuration(com.google.protobuf.Duration.newBuilder().setSeconds(60).build())
                        .build()
                )
                .build()

            val notificationChannel = "projects/$projectId/notificationChannels/1234567890"
            val alertPolicy = AlertPolicy.newBuilder()
                .setDisplayName("High CPU Usage Alert")
                .addConditions(condition)
                .addNotificationChannels(notificationChannel)
                .setCombiner(AlertPolicy.ConditionCombinerType.AND)
                .build()

            val createAlertPolicyRequest = CreateAlertPolicyRequest.newBuilder()
                .setName(projectName.toString())
                .setAlertPolicy(alertPolicy)
                .build()

            alertPolicyServiceClient.createAlertPolicy(createAlertPolicyRequest)
            println("Alert policy created.")
        }
    }

    fun listAlertPolicies() {
        AlertPolicyServiceClient.create().use { alertPolicyServiceClient ->
            val projectName = ProjectName.of(projectId)
            val response = alertPolicyServiceClient.listAlertPolicies(projectName.toString())
            response.iterateAll().forEach { alertPolicy ->
                println("Alert Policy Name: ${alertPolicy.name}")
                println("Display Name: ${alertPolicy.displayName}")
                println("Conditions: ${alertPolicy.conditionsList}")
                println("Notification Channels: ${alertPolicy.notificationChannelsList}")
                println()
            }
        }
    }
}
