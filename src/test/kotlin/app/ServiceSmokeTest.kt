package app

import com.google.gson.Gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import microservices.User

class ServiceSmokeTest {
    private val gson = Gson()

    @Test
    fun `user lifecycle works`() {
        withTestApplication({ userModule(InMemoryDataStore()) }) {
            val createCall = handleRequest(HttpMethod.Post, "/users") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{\"name\":\"Jane\",\"email\":\"jane@example.com\"}")
            }
            assertEquals(HttpStatusCode.Created, createCall.response.status())
            val created = gson.fromJson(createCall.response.content, User::class.java)
            assertEquals("Jane", created.name)

            val listCall = handleRequest(HttpMethod.Get, "/users") {}
            val users: Array<User> = gson.fromJson(listCall.response.content, Array<User>::class.java)
            assertTrue(users.any { it.email == "jane@example.com" })
        }
    }

    @Test
    fun `auth succeeds with default credentials`() {
        withTestApplication({ authModule(InMemoryDataStore()) }) {
            val loginCall = handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{\"username\":\"user1\",\"password\":\"password1\"}")
            }
            assertEquals(HttpStatusCode.OK, loginCall.response.status())
            assertTrue(loginCall.response.content!!.contains("token_for_user1"))
        }
    }
}
