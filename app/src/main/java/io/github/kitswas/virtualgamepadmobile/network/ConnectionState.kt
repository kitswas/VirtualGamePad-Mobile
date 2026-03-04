package io.github.kitswas.virtualgamepadmobile.network


data class ConnectionState(
    val connected: Boolean = false,
    val ipAddress: String = "",
    val port: Int = -1,
    val error: String? = null,
    val isConnecting: Boolean = false,
    val isRunningDiagnostics: Boolean = false,
    val diagnosticResults: List<NetworkDiagnostics.DiagnosticResult> = emptyList()
)
