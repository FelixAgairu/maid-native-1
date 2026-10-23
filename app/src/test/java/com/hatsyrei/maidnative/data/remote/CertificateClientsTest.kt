package com.hatsyrei.maidnative.data.remote

import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test
import java.net.InetSocketAddress
import java.nio.file.Files
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

class CertificateClientsTest {
    @Test fun `opt in trusts a configured CA but still rejects wrong host and opt out restores system trust`() {
        val dir = Files.createTempDirectory("maid-tls-test")
        val storeFile = dir.resolve("server.p12")
        val keytool = java.nio.file.Path.of(System.getProperty("java.home"), "bin", "keytool").toString()
        val process = ProcessBuilder(keytool, "-genkeypair", "-alias", "test", "-keyalg", "RSA",
            "-storetype", "PKCS12", "-keystore", storeFile.toString(), "-storepass", "changeit",
            "-keypass", "changeit", "-dname", "CN=localhost", "-validity", "2", "-ext", "SAN=dns:localhost")
            .redirectErrorStream(true).start()
        val diagnostic = process.inputStream.bufferedReader().readText()
        assertEquals(diagnostic, 0, process.waitFor())
        val serverStore = KeyStore.getInstance("PKCS12").apply {
            Files.newInputStream(storeFile).use { load(it, "changeit".toCharArray()) }
        }
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("test", serverStore.getCertificate("test"))
        }
        val keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(serverStore, "changeit".toCharArray())
        }
        val tls = SSLContext.getInstance("TLS").apply { init(keys.keyManagers, null, null) }
        val server = HttpsServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            httpsConfigurator = HttpsConfigurator(tls)
            createContext("/") { exchange ->
                exchange.sendResponseHeaders(200, 2)
                exchange.responseBody.use { it.write("ok".toByteArray()) }
            }
            start()
        }
        val base = OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS)
            .proxy(java.net.Proxy.NO_PROXY)
            .dns { listOf(java.net.InetAddress.getByName("127.0.0.1")) }
            .build()
        val clients = CertificateClients(base) { trustStore }
        fun get(client: OkHttpClient, host: String) = client.newCall(
            Request.Builder().url("https://$host:${server.address.port}/").build(),
        ).execute().use { it.body.string() }
        try {
            assertSame(base, clients.get(false))
            assertThrows(SSLHandshakeException::class.java) { get(clients.get(false), "localhost") }
            val optedIn = clients.get(true)
            assertEquals("ok", get(optedIn, "localhost"))
            assertThrows(SSLPeerUnverifiedException::class.java) { get(optedIn, "127.0.0.1") }
            assertSame(base, clients.get(false))
            assertThrows(SSLHandshakeException::class.java) { get(clients.get(false), "localhost") }
            assertNotSame(optedIn, clients.get(true))
        } finally {
            server.stop(0)
            base.connectionPool.evictAll()
            clients.get(false)
            Files.deleteIfExists(storeFile)
            Files.deleteIfExists(dir)
        }
    }
}
