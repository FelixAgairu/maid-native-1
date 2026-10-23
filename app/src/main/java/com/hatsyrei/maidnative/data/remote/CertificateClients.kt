package com.hatsyrei.maidnative.data.remote

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/** A separate connection pool prevents an opt-in TLS connection being reused after opting out. */
internal class CertificateClients(
    private val system: OkHttpClient,
    private val userStore: () -> KeyStore = {
        KeyStore.getInstance("AndroidCAStore").apply { load(null, null) }
    },
) {
    private var user: OkHttpClient? = null

    @Synchronized
    fun get(trustUserCertificates: Boolean): OkHttpClient {
        if (!trustUserCertificates) {
            user?.connectionPool?.evictAll()
            user = null
            return system
        }
        return user ?: withTrustStore(system, userStore()).also { user = it }
    }

    companion object {
        /** AndroidCAStore contains the enabled system CAs and user-installed CAs. */
        internal fun withTrustStore(base: OkHttpClient, store: KeyStore): OkHttpClient {
            val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            factory.init(store)
            val trust = factory.trustManagers.filterIsInstance<X509TrustManager>().single()
            val tls = SSLContext.getInstance("TLS").apply { init(null, arrayOf(trust), null) }
            return base.newBuilder()
                .connectionPool(ConnectionPool())
                .sslSocketFactory(tls.socketFactory, trust)
                // The default hostname verifier remains in force. There is no trust-all mode.
                .build()
        }
    }
}
