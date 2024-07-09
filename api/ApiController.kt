package api

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object ApiController {
    fun start() {
        embeddedServer(Netty, port = 8081) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                get("/status") {
                    call.respond(mapOf("status" to "API is running"))
                }
            }
        }.start(wait = true)
    }
}
