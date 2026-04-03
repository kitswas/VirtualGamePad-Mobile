package io.github.kitswas.virtualgamepadmobile.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class NetworkDiagnosticsTest {

    private fun ipToBytes(ip: String): ByteArray {
        return InetAddress.getByName(ip).address
    }

    @Test
    fun testIsSameSubnet_Logic() {
        // Standard /24 subnet
        val prefix24 = 24
        assertTrue(NetworkDiagnostics.isSameSubnet(ipToBytes("192.168.1.5"), ipToBytes("192.168.1.10"), prefix24))
        assertFalse(NetworkDiagnostics.isSameSubnet(ipToBytes("192.168.1.5"), ipToBytes("192.168.2.5"), prefix24))

        // Standard /16 subnet
        val prefix16 = 16
        assertTrue(NetworkDiagnostics.isSameSubnet(ipToBytes("10.0.1.5"), ipToBytes("10.0.50.10"), prefix16))
        assertFalse(NetworkDiagnostics.isSameSubnet(ipToBytes("10.0.1.5"), ipToBytes("10.1.1.5"), prefix16))

        // Edge case: /32 (exact match required)
        val prefix32 = 32
        assertTrue(NetworkDiagnostics.isSameSubnet(ipToBytes("192.168.1.1"), ipToBytes("192.168.1.1"), prefix32))
        assertFalse(NetworkDiagnostics.isSameSubnet(ipToBytes("192.168.1.1"), ipToBytes("192.168.1.2"), prefix32))

        // Edge case: /0 (all match)
        val prefix0 = 0
        assertTrue(NetworkDiagnostics.isSameSubnet(ipToBytes("192.168.1.1"), ipToBytes("10.0.0.1"), prefix0))
        
        // Complex mask: /20 (255.255.240.0)
        // 172.16.0.0 to 172.16.15.255
        val prefix20 = 20
        assertTrue(NetworkDiagnostics.isSameSubnet(ipToBytes("172.16.0.1"), ipToBytes("172.16.15.254"), prefix20))
        assertFalse(NetworkDiagnostics.isSameSubnet(ipToBytes("172.16.0.1"), ipToBytes("172.16.16.1"), prefix20))
    }

    @Test
    fun testIsSameSubnet_InvalidInput() {
        assertFalse(NetworkDiagnostics.isSameSubnet(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 3, 4), 24))
        assertFalse(NetworkDiagnostics.isSameSubnet(byteArrayOf(1, 2, 3, 4), byteArrayOf(1, 2, 3), 24))
    }
}
