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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

data class User(val id: Int, val name: String, val email: String)

object Users : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 50)
    val email = varchar("email", 50)
}

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/mydatabase"
            driverClassName = "org.postgresql.Driver"
            username = "dbuser"
            password = "dbpassword"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            create(Users)
        }
    }
}

object UserService {
    init {
        DatabaseFactory.init()
    }

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
                        val users = transaction {
                            Users.selectAll().map {
                                User(
                                    id = it[Users.id],
                                    name = it[Users.name],
                                    email = it[Users.email]
                                )
                            }
                        }
                        call.respond(users)
                    }

                    post {
                        val user = call.receive<User>()
                        transaction {
                            Users.insert {
                                it[name] = user.name
                                it[email] = user.email
                            }
                        }
                        call.respond(HttpStatusCode.Created, user)
                    }

                    delete("/{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id != null) {
                            transaction {
                                Users.deleteWhere { Users.id eq id }
                            }
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
