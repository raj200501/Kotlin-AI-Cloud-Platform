package cloud_management

import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Node

object ResourceAllocation {
    private val api = CoreV1Api()

    fun allocateResources(): Map<String, Any> {
        val nodes = api.listNode(null, null, null, null, null, null, null, null, null, false)
        val allocations = mutableMapOf<String, Any>()
        for (node in nodes.items) {
            allocations[node.metadata!!.name!!] = node.status!!.allocatable
        }
        return allocations
    }
}
