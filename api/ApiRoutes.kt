package api

import io.ktor.application.*
import io.ktor.routing.*

object ApiRoutes {
    fun Application.apiRoutes() {
        routing {
            route("/api") {
                get("/health") {
                    call.respond(mapOf("status" to "API is healthy"))
                }
            }
        }
    }
}
