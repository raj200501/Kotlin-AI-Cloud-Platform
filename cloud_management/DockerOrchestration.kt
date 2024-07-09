package cloud_management

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DockerClientBuilder

object DockerOrchestration {
    private val dockerClient: DockerClient = DockerClientBuilder.getInstance().build()

    fun listContainers(): List<String> {
        return dockerClient.listContainersCmd().exec().map { it.names.joinToString(", ") }
    }

    fun startContainer(containerId: String) {
        dockerClient.startContainerCmd(containerId).exec()
    }

    fun stopContainer(containerId: String) {
        dockerClient.stopContainerCmd(containerId).exec()
    }
}
