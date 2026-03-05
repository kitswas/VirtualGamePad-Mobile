package io.github.kitswas.virtualgamepadmobile.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

class NetworkDiagnostics(private val context: Context) {

    private val tag = "NetworkDiagnostics"

    data class DiagnosticResult(
        val step: DiagnosticStep,
        val isPassed: Boolean,
        val message: String,
        val detail: String? = null
    )

    enum class DiagnosticStep {
        WIFI,
        IP,
        SUBNET,
        PING,
        PORT
    }

    /**
     * Checks if the device has an active Wi-Fi or Ethernet connection.
     */
    fun checkNetworkConnectivity(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Retrieves the local IPv4 address of the device.
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (addr != null && !addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) return sAddr
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting local IP", e)
        }
        return null
    }

    /**
     * Checks if two IP addresses are on the same subnet.
     */
    fun isSameSubnet(ip1: String, ip2: String): Boolean {
        return try {
            val addr1 = InetAddress.getByName(ip1)
            val addr2 = InetAddress.getByName(ip2)
            val b1 = addr1.address
            val b2 = addr2.address

            if (b1.size != 4 || b2.size != 4) return false

            val prefix = NetworkInterface.getByInetAddress(addr1)
                ?.interfaceAddresses?.firstOrNull { it.address == addr1 }
                ?.networkPrefixLength?.toInt() ?: return false

            isSameSubnet(b1, b2, prefix)
        } catch (e: Exception) {
            Log.e(tag, "Error checking subnet", e)
            false
        }
    }

    /**
     * Attempts to ping the target host.
     */
    suspend fun pingHost(host: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(host)
            address.isReachable(2000) // 2 second timeout
        } catch (e: IOException) {
            Log.e(tag, "Ping failed", e)
            false
        }
    }

    /**
     * Attempts to connect to the target host and port.
     */
    suspend fun checkPort(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 2000) // 2 second timeout
            socket.close()
            true
        } catch (e: IOException) {
            Log.e(tag, "Port check failed", e)
            false
        }
    }

    companion object {
        /**
         * Pure bitwise check if two IPv4 byte arrays are on the same subnet.
         */
        fun isSameSubnet(b1: ByteArray, b2: ByteArray, prefix: Int): Boolean {
            if (b1.size != 4 || b2.size != 4) return false
            val mask = if (prefix == 0) 0 else -1 shl (32 - prefix)
            val i1 =
                ((b1[0].toInt() and 0xFF) shl 24) or ((b1[1].toInt() and 0xFF) shl 16) or ((b1[2].toInt() and 0xFF) shl 8) or (b1[3].toInt() and 0xFF)
            val i2 =
                ((b2[0].toInt() and 0xFF) shl 24) or ((b2[1].toInt() and 0xFF) shl 16) or ((b2[2].toInt() and 0xFF) shl 8) or (b2[3].toInt() and 0xFF)
            return (i1 and mask) == (i2 and mask)
        }
    }
}
