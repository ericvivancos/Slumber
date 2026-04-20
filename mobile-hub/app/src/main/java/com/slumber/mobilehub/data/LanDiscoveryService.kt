package com.slumber.mobilehub.data

import com.slumber.mobilehub.domain.model.SlumberServiceEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL

data class DiscoveryScanResult(
    val devices: List<SlumberServiceEndpoint>
)

class LanDiscoveryService {
    suspend fun discoverServices(port: Int = DEFAULT_PORT): DiscoveryScanResult {
        val candidateHosts = buildCandidateHosts()
        if (candidateHosts.isEmpty()) {
            return DiscoveryScanResult(devices = emptyList())
        }

        val semaphore = Semaphore(MAX_PARALLEL_REQUESTS)
        val devices = coroutineScope {
            candidateHosts.map { host ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        fetchIdentity(host, port)
                    }
                }
            }.awaitAll().filterNotNull()
        }.distinctBy { it.id }.sortedBy { it.deviceName }

        return DiscoveryScanResult(devices = devices)
    }

    private suspend fun fetchIdentity(host: String, port: Int): SlumberServiceEndpoint? {
        return withContext(Dispatchers.IO) {
            val connection = (URL("http://$host:$port/identity").openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "GET"
            }

            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }

                val body = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }

                val json = JSONObject(body)
                val capabilitiesJson = json.optJSONArray("capabilities") ?: JSONArray()
                val capabilities = buildList {
                    for (index in 0 until capabilitiesJson.length()) {
                        add(capabilitiesJson.getString(index))
                    }
                }

                SlumberServiceEndpoint(
                    id = json.optString("deviceId", "$host:$port"),
                    deviceName = json.optString("deviceName", host),
                    host = json.optString("host", host),
                    port = json.optInt("port", port),
                    serviceVersion = json.optString("serviceVersion", "unknown"),
                    capabilities = capabilities,
                    availability = json.optString("availability", "unknown"),
                    isLinked = false
                )
            } catch (_: Exception) {
                null
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun buildCandidateHosts(): List<String> {
        val hosts = linkedSetOf(
            EMULATOR_HOST_ALIAS,
            GENYMOTION_HOST_ALIAS
        )

        val subnetPrefix = findSubnetPrefix()
        if (subnetPrefix != null && !isEmulatorSubnet(subnetPrefix)) {
            for (hostSuffix in 1..254) {
                hosts += "$subnetPrefix.$hostSuffix"
            }
        }

        return hosts.toList()
    }

    private fun findSubnetPrefix(): String? {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
        val candidates = mutableListOf<InterfaceCandidate>()

        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (!networkInterface.isUp || networkInterface.isLoopback || networkInterface.isVirtual) {
                continue
            }

            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (address is Inet4Address && address.isSiteLocalAddress) {
                    val hostAddress = address.hostAddress ?: continue
                    val subnetPrefix = hostAddress.substringBeforeLast('.')
                    candidates += InterfaceCandidate(
                        priority = interfacePriority(networkInterface.name, hostAddress),
                        interfaceName = networkInterface.name,
                        hostAddress = hostAddress,
                        subnetPrefix = subnetPrefix
                    )
                }
            }
        }

        return candidates.minByOrNull { it.priority }?.subnetPrefix
    }

    private fun isEmulatorSubnet(subnetPrefix: String): Boolean {
        return subnetPrefix == "10.0.2" || subnetPrefix == "10.0.3"
    }

    private fun interfacePriority(interfaceName: String, hostAddress: String): Int {
        val normalizedName = interfaceName.lowercase()

        return when {
            normalizedName.startsWith("wlan") -> 0
            normalizedName.startsWith("eth") -> 1
            hostAddress.startsWith("192.168.") -> 2
            hostAddress.startsWith("172.") -> 3
            normalizedName.startsWith("rmnet") || normalizedName.startsWith("ccmni") -> 10
            else -> 5
        }
    }

    private data class InterfaceCandidate(
        val priority: Int,
        val interfaceName: String,
        val hostAddress: String,
        val subnetPrefix: String
    )

    private companion object {
        const val DEFAULT_PORT = 34821
        const val EMULATOR_HOST_ALIAS = "10.0.2.2"
        const val GENYMOTION_HOST_ALIAS = "10.0.3.2"
        const val CONNECT_TIMEOUT_MS = 600
        const val READ_TIMEOUT_MS = 800
        const val MAX_PARALLEL_REQUESTS = 24
    }
}
