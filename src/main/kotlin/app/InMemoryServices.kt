package app

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import microservices.AuthResponse
import microservices.Credentials
import microservices.Notification
import microservices.Payment
import microservices.User
import java.util.concurrent.atomic.AtomicInteger

internal data class NewUser(val name: String, val email: String)
internal data class NewPayment(val amount: Double, val userId: Int)
internal data class NewNotification(val message: String, val userId: Int)

internal class InMemoryDataStore(
    initialUsers: List<User> = emptyList(),
    private val validUsers: Map<String, String> = mapOf("user1" to "password1", "user2" to "password2")
) {
    private val userIdSequence = AtomicInteger(initialUsers.size)
    private val paymentIdSequence = AtomicInteger()
    private val notificationIdSequence = AtomicInteger()
    private val users = mutableListOf<User>().apply { addAll(initialUsers) }
    private val payments = mutableListOf<Payment>()
    private val notifications = mutableListOf<Notification>()

    fun listUsers(): List<User> = users.toList()

    fun addUser(request: NewUser): User {
        val user = User(userIdSequence.incrementAndGet(), request.name, request.email)
        users.add(user)
        return user
    }

    fun deleteUser(id: Int): Boolean = users.removeIf { it.id == id }

    fun listPayments(): List<Payment> = payments.toList()

    fun addPayment(request: NewPayment): Payment {
        val payment = Payment(paymentIdSequence.incrementAndGet(), request.amount, request.userId)
        payments.add(payment)
        return payment
    }

    fun listNotifications(): List<Notification> = notifications.toList()

    fun addNotification(request: NewNotification): Notification {
        val notification = Notification(notificationIdSequence.incrementAndGet(), request.message, request.userId)
        notifications.add(notification)
        return notification
    }

    fun authenticate(credentials: Credentials): AuthResponse? {
        val isValid = validUsers[credentials.username] == credentials.password
        return if (isValid) AuthResponse(token = "token_for_${credentials.username}") else null
    }
}

private fun Application.installJson() {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
}

fun Application.userModule(store: InMemoryDataStore = InMemoryDataStore()) {
    installJson()
    routing {
        route("/users") {
            get {
                call.respond(store.listUsers())
            }

            post {
                val request = call.receive<NewUser>()
                val created = store.addUser(request)
                call.respond(HttpStatusCode.Created, created)
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user id"))
                } else {
                    val removed = store.deleteUser(id)
                    if (removed) {
                        call.respond(HttpStatusCode.NoContent, Unit)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                }
            }
        }
    }
}

fun Application.authModule(store: InMemoryDataStore = InMemoryDataStore()) {
    installJson()
    routing {
        post("/login") {
            val credentials = call.receive<Credentials>()
            val response = store.authenticate(credentials)
            if (response != null) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }
    }
}

fun Application.paymentModule(store: InMemoryDataStore = InMemoryDataStore()) {
    installJson()
    routing {
        route("/payments") {
            get {
                call.respond(store.listPayments())
            }

            post {
                val request = call.receive<NewPayment>()
                val created = store.addPayment(request)
                call.respond(HttpStatusCode.Created, created)
            }
        }
    }
}

fun Application.notificationModule(store: InMemoryDataStore = InMemoryDataStore()) {
    installJson()
    routing {
        route("/notifications") {
            get {
                call.respond(store.listNotifications())
            }

            post {
                val request = call.receive<NewNotification>()
                val created = store.addNotification(request)
                call.respond(HttpStatusCode.Created, created)
            }
        }
    }
}
