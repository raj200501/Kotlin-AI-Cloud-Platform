package app

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.CountDownLatch

fun main() {
    val sharedStore = InMemoryDataStore()
    val servers = listOf(
        embeddedServer(Netty, port = 8080) { userModule(sharedStore) },
        embeddedServer(Netty, port = 8081) { authModule(sharedStore) },
        embeddedServer(Netty, port = 8082) { paymentModule(sharedStore) },
        embeddedServer(Netty, port = 8083) { notificationModule(sharedStore) }
    )

    servers.forEach { it.start(wait = false) }
    println("User Service running at http://localhost:8080/users")
    println("Auth Service running at http://localhost:8081/login")
    println("Payment Service running at http://localhost:8082/payments")
    println("Notification Service running at http://localhost:8083/notifications")

    Runtime.getRuntime().addShutdownHook(Thread {
        servers.forEach { server -> server.stop(1000, 5000) }
    })

    CountDownLatch(1).await()
}
