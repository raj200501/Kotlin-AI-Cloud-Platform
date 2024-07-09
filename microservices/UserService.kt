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

data class User(val id: Int, val name: String, val email: String)

object UserService {
    private val users = mutableListOf<User>()

    fun start() {
        embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                route("/users") {
                    get {
                        call.respond(users)
                    }

                    post {
                        val user = call.receive<User>()
                        users.add(user)
                        call.respond(HttpStatusCode.Created, user)
                    }

                    delete("/{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id != null) {
                            users.removeIf { it.id == id }
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
        }.start(wait = true)
    }
}
