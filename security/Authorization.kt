package security

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.jwt
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object Authorization {
    fun start() {
        embeddedServer(Netty, port = 8083) {
            install(Authentication) {
                jwt {
                    verifier(JwtConfig.verifier)
                    validate {
                        if (it.payload.audience.contains("ktor.io")) {
                            JWTPrincipal(it.payload)
                        } else {
                            null
                        }
                    }
                }
            }

            routing {
                authenticate {
                    get("/admin") {
                        call.respond(mapOf("message" to "Admin access granted"))
                    }
                }
            }
        }.start(wait = true)
    }
}
