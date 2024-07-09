package microservices

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

data class Notification(val id: Int, val message: String, val userId: Int)

object NotificationService {
    private val notifications = mutableListOf<Notification>()

    fun start() {
        embeddedServer(Netty, port = 8083) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                route("/notifications") {
                    get {
                        call.respond(notifications)
                    }

                    post {
                        val notification = call.receive<Notification>()
                        notifications.add(notification)
                        call.respond(HttpStatusCode.Created, notification)
                    }
                }
            }
        }.start(wait = true)
    }
}
