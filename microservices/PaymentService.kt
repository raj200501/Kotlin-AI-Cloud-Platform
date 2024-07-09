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

data class Payment(val id: Int, val amount: Double, val userId: Int)

object PaymentService {
    private val payments = mutableListOf<Payment>()

    fun start() {
        embeddedServer(Netty, port = 8082) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }

            routing {
                route("/payments") {
                    get {
                        call.respond(payments)
                    }

                    post {
                        val payment = call.receive<Payment>()
                        payments.add(payment)
                        call.respond(HttpStatusCode.Created, payment)
                    }
                }
            }
        }.start(wait = true)
    }
}
