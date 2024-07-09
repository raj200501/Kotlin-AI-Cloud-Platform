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

data class Credentials(val username: String, val password: String)
data class AuthResponse(val token: String)

object AuthService {
    private val validUsers = mapOf("user1" to "password1", "user2" to "password2")

    fun start() {
        embeddedServer(Netty, port = 8081) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                post("/login") {
                    val credentials = call.receive<Credentials>()
                    if (validUsers[credentials.username] == credentials.password) {
                        call.respond(AuthResponse(token = generateToken(credentials.username)))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                }
            }
        }.start(wait = true)
    }

    private fun generateToken(username: String): String {
        // Mock token generation
        return "token_for_$username"
    }
}
