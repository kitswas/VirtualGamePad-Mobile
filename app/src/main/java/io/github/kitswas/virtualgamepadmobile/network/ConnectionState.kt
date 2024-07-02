package io.github.kitswas.virtualgamepadmobile.network

import java.net.Socket


data class ConnectionState(
    var connected: Boolean = false,
    val ipAddress: String = "",
    val port: Int = -1,
    var socket: Socket? = null
)
