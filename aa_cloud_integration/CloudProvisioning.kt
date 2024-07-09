package aa_cloud_integration

import com.google.api.gax.longrunning.OperationFuture
import com.google.api.gax.rpc.ApiException
import com.google.cloud.compute.v1.*
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import java.nio.file.Files
import java.nio.file.Paths

object CloudProvisioning {

    private val computeClient = InstancesClient.create()
    private val projectId = "your-gcp-project-id"
    private val zone = "us-central1-a"

    @Throws(ApiException::class)
    fun createComputeInstance(instanceName: String, machineType: String, sourceImage: String, diskSizeGb: Long): String {
        val machineTypeUrl = "zones/$zone/machineTypes/$machineType"
        val sourceImageFamily = "projects/debian-cloud/global/images/family/$sourceImage"

        val instance = Instance.newBuilder()
            .setName(instanceName)
            .setMachineType(machineTypeUrl)
            .addDisks(
                AttachedDisk.newBuilder()
                    .setBoot(true)
                    .setInitializeParams(
                        AttachedDiskInitializeParams.newBuilder()
                            .setSourceImage(sourceImageFamily)
                            .setDiskSizeGb(diskSizeGb)
                            .build()
                    )
                    .build()
            )
            .addNetworkInterfaces(
                NetworkInterface.newBuilder()
                    .setName("global/networks/default")
                    .build()
            )
            .build()

        val insertInstanceRequest = InsertInstanceRequest.newBuilder()
            .setProject(projectId)
            .setZone(zone)
            .setInstanceResource(instance)
            .build()

        val operation: OperationFuture<Operation, Operation> = computeClient.insertAsync(insertInstanceRequest)
        return operation.name
    }

    fun waitForOperationCompletion(operationName: String) {
        var operation: Operation
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

    fun createStorageBucket(bucketName: String) {
        val storage = StorageOptions.getDefaultInstance().service
        val bucket = storage.create(com.google.cloud.storage.BucketInfo.of(bucketName))
        println("Bucket ${bucket.name} created.")
    }

    fun uploadFileToBucket(bucketName: String, fileName: String, filePath: String) {
        val storage = StorageOptions.getDefaultInstance().service
        val blobId = BlobId.of(bucketName, fileName)
        val blobInfo = BlobInfo.newBuilder(blobId).build()
        val fileBytes = Files.readAllBytes(Paths.get(filePath))
        storage.create(blobInfo, fileBytes)
        println("File $fileName uploaded to bucket $bucketName.")
    }

    fun listInstances(): List<Instance> {
        val instances = mutableListOf<Instance>()
        val listInstancesRequest = ListInstancesRequest.newBuilder()
            .setProject(projectId)
            .setZone(zone)
            .build()

        for (instance in computeClient.list(listInstancesRequest).iterateAll()) {
            instances.add(instance)
        }
        return instances
    }

    fun deleteInstance(instanceName: String) {
        val deleteInstanceRequest = DeleteInstanceRequest.newBuilder()
            .setProject(projectId)
            .setZone(zone)
            .setInstance(instanceName)
            .build()
        val operation: OperationFuture<Operation, Operation> = computeClient.deleteAsync(deleteInstanceRequest)
        waitForOperationCompletion(operation.name)
    }

    fun getInstanceDetails(instanceName: String): Instance? {
        return computeClient.get(projectId, zone, instanceName)
    }

    fun listBuckets(): List<String> {
        val storage = StorageOptions.getDefaultInstance().service
        val buckets = mutableListOf<String>()
        for (bucket in storage.list().iterateAll()) {
            buckets.add(bucket.name)
        }
        return buckets
    }

    fun downloadFileFromBucket(bucketName: String, fileName: String, destinationPath: String) {
        val storage = StorageOptions.getDefaultInstance().service
        val blob = storage.get(BlobId.of(bucketName, fileName))
        blob.downloadTo(Paths.get(destinationPath))
        println("File $fileName downloaded to $destinationPath.")
    }

    fun deleteFileFromBucket(bucketName: String, fileName: String) {
        val storage = StorageOptions.getDefaultInstance().service
        storage.delete(BlobId.of(bucketName, fileName))
        println("File $fileName deleted from bucket $bucketName.")
    }

    fun deleteBucket(bucketName: String) {
        val storage = StorageOptions.getDefaultInstance().service
        storage.delete(bucketName)
        println("Bucket $bucketName deleted.")
    }

    fun addInstanceTags(instanceName: String, tags: List<String>) {
        val tagsResource = Tags.newBuilder().addAllItems(tags).build()
        val setTagsRequest = SetTagsInstanceRequest.newBuilder()
            .setProject(projectId)
            .setZone(zone)
            .setInstance(instanceName)
            .setTagsResource(tagsResource)
            .build()

        val operation: OperationFuture<Operation, Operation> = computeClient.setTagsAsync(setTagsRequest)
        waitForOperationCompletion(operation.name)
    }

    fun setInstanceLabels(instanceName: String, labels: Map<String, String>) {
        val setLabelsRequest = SetLabelsInstanceRequest.newBuilder()
            .setProject(projectId)
            .setZone(zone)
            .setInstance(instanceName)
            .putAllLabels(labels)
            .build()

        val operation: OperationFuture<Operation, Operation> = computeClient.setLabelsAsync(setLabelsRequest)
        waitForOperationCompletion(operation.name)
    }
}
