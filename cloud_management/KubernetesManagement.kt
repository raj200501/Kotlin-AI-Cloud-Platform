package cloud_management

import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Config

object KubernetesManagement {
    private val client: ApiClient = Config.defaultClient()

    init {
        Configuration.setDefaultApiClient(client)
    }

    fun listPods(): List<String> {
        val api = CoreV1Api()
        val pods = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, false)
        return pods.items.map { it.metadata!!.name!! }
    }
}
