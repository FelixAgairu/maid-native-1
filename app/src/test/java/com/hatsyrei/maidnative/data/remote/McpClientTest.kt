package com.hatsyrei.maidnative.data.remote

import com.hatsyrei.maidnative.domain.tools.McpException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class McpClientTest {

    private val client = McpClient()

    private fun response(body: String, type: String?, code: Int = 200) = Response.Builder()
        .request(Request.Builder().url("http://127.0.0.1:8000/mcp").build())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code in 200..299) "OK" else "Bad")
        .body(body.toResponseBody(type?.toMediaType()))
        .build()

    @Test
    fun `json response yields its result`() {
        val result = client.read(response("""{"jsonrpc":"2.0","id":3,"result":{"tools":[]}}""", "application/json"), "3", false)
        assertEquals(0, result!!.getJSONArray("tools").length())
    }

    @Test
    fun `sse response skips other messages until the answer`() {
        val stream = "event: message\r\ndata: {\"jsonrpc\":\"2.0\",\"method\":\"notifications/progress\"}\r\n\r\n" +
            ": comment\n" +
            "data: {\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}\n\n" +
            "event: message\ndata: {\"jsonrpc\":\"2.0\",\ndata: \"id\":7,\"result\":{\"ok\":true}}\n\n"
        val result = client.read(response(stream, "text/event-stream"), "7", true)
        assertEquals(true, result!!.getBoolean("ok"))
    }

    @Test
    fun `last event without a blank line still counts`() {
        val message = client.answer(Buffer().writeUtf8("data: {\"id\":2,\"result\":{}}"), "2")
        assertEquals(2, message.getInt("id"))
    }

    @Test
    fun `stream that ends unanswered is an error`() {
        assertThrows(McpException::class.java) {
            client.answer(Buffer().writeUtf8("data: {\"id\":1,\"result\":{}}\n\n"), "2")
        }
    }

    @Test
    fun `json-rpc error becomes its message`() {
        val error = assertThrows(McpException::class.java) {
            client.read(response("""{"id":1,"error":{"code":-32602,"message":"Invalid params"}}""", "application/json"), "1", false)
        }
        assertEquals("Invalid params", error.message)
    }

    @Test
    fun `http failure carries the server's reason`() {
        val error = assertThrows(McpException::class.java) {
            client.read(response("""{"error":{"code":-32000,"message":"Missing session ID"}}""", "application/json", 400), "1", false)
        }
        assertEquals("HTTP 400 Bad: Missing session ID", error.message)
    }

    @Test
    fun `404 inside a session asks for a new one`() {
        assertThrows(McpClient.SessionExpired::class.java) {
            client.read(response("", "text/plain", 404), "1", true)
        }
        assertThrows(McpException::class.java) { client.read(response("", "text/plain", 404), "1", false) }
    }

    @Test
    fun `notification has no result`() {
        assertNull(client.read(response("", "application/json", 202), null, true))
    }
}
