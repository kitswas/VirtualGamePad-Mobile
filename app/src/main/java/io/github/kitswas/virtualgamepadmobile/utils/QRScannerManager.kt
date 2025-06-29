package io.github.kitswas.virtualgamepadmobile.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface for QR scanner management functionality, designed for testing and dependency injection
 */
interface QRScannerManagerInterface {
    val moduleAvailabilityState: StateFlow<QRScannerManager.ModuleAvailability>
    fun getModuleAvailability(): QRScannerManager.ModuleAvailability
    fun getQRScanner(): GmsBarcodeScanner?
    fun areModulesAvailable(): Boolean
    fun startInstallation(
        onProgress: ((Int) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
    )

    fun cancelInstallation()
}

class QRScannerManager(private val context: Context) : QRScannerManagerInterface {

    private var scanner: GmsBarcodeScanner? = null
    private val _moduleAvailabilityState =
        MutableStateFlow<ModuleAvailability>(ModuleAvailability.CHECKING)
    override val moduleAvailabilityState: StateFlow<ModuleAvailability> =
        _moduleAvailabilityState.asStateFlow()

    private var currentInstallListener: InstallStatusListener? = null
    private var onProgressCallback: ((Int) -> Unit)? = null
    private var onCompleteCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    sealed class ModuleAvailability {
        object CHECKING : ModuleAvailability()
        object AVAILABLE : ModuleAvailability()
        object NOT_AVAILABLE : ModuleAvailability()
        object API_UNAVAILABLE : ModuleAvailability()
        object INSTALLING : ModuleAvailability()
        object INSTALL_FAILED : ModuleAvailability()
        object INSTALL_CANCELLED : ModuleAvailability()
    }

    init {
        checkModuleAvailability()
    }

    private fun checkModuleAvailability() {
        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
        ).build()
        scanner = GmsBarcodeScanning.getClient(context, options)

        val moduleInstallClient = ModuleInstall.getClient(context)
        moduleInstallClient.areModulesAvailable(scanner!!).addOnSuccessListener { result ->
            if (result.areModulesAvailable()) {
                _moduleAvailabilityState.value = ModuleAvailability.AVAILABLE
                Log.d("QRScannerManager", "Modules are available")
            } else {
                _moduleAvailabilityState.value = ModuleAvailability.NOT_AVAILABLE
                Log.i("QRScannerManager", "Modules not found on device")
            }
        }.addOnFailureListener { exception ->
            Log.w("QRScannerManager", "Module detection failed: ${exception.message}")
            // Check if it's an API unavailable error
            if (exception.message?.contains("API_UNAVAILABLE") == true ||
                exception.message?.contains("not available on this device") == true
            ) {
                _moduleAvailabilityState.value = ModuleAvailability.API_UNAVAILABLE
                Log.w(
                    "QRScannerManager",
                    "ModuleInstall API is not available - likely Google Play Services issue"
                )
            } else {
                // For other failures, treat as modules not available (can still try to install)
                _moduleAvailabilityState.value = ModuleAvailability.NOT_AVAILABLE
                Log.w(
                    "QRScannerManager",
                    "Module availability check failed, but installation may still work"
                )
            }
        }
    }

    /**
     * Returns the scanner if available, null if modules need to be installed
     */
    override fun getQRScanner(): GmsBarcodeScanner? {
        return when (_moduleAvailabilityState.value) {
            ModuleAvailability.AVAILABLE -> {
                Log.d("QRScannerManager", "Returning available scanner")
                scanner
            }

            else -> {
                Log.d("QRScannerManager", "Scanner not available, modules need installation")
                null
            }
        }
    }

    /**
     * Checks if modules are available without installing
     */
    override fun areModulesAvailable(): Boolean {
        return _moduleAvailabilityState.value == ModuleAvailability.AVAILABLE
    }

    /**
     * Starts the module installation process
     */
    override fun startInstallation(
        onProgress: ((Int) -> Unit)?,
        onComplete: (() -> Unit)?,
        onError: ((String) -> Unit)?,
    ) {
        // Check if API is available before attempting installation
        if (_moduleAvailabilityState.value == ModuleAvailability.API_UNAVAILABLE) {
            Log.d("QRScannerManager", "Cannot install - API unavailable")
            onError?.invoke("QR Scanner is not available. Please update Google Play Services and try again.")
            return
        }

        if (_moduleAvailabilityState.value == ModuleAvailability.INSTALLING) {
            Log.d("QRScannerManager", "Installation already in progress")
            return
        }

        onProgressCallback = onProgress
        onCompleteCallback = onComplete
        onErrorCallback = onError

        _moduleAvailabilityState.value = ModuleAvailability.INSTALLING

        val moduleInstallClient = ModuleInstall.getClient(context)

        currentInstallListener = object : InstallStatusListener {
            override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
                update.progressInfo?.let { progressInfo ->
                    val progress =
                        (progressInfo.bytesDownloaded * 100 / progressInfo.totalBytesToDownload).toInt()
                    Log.d("QRScannerManager", "Module download progress: $progress%")
                    onProgress?.invoke(progress)
                }

                if (isTerminateState(update.installState)) {
                    when (update.installState) {
                        InstallState.STATE_COMPLETED -> {
                            Log.i("QRScannerManager", "Module installation completed")
                            _moduleAvailabilityState.value = ModuleAvailability.AVAILABLE
                            onComplete?.invoke()
                        }

                        InstallState.STATE_CANCELED -> {
                            Log.i("QRScannerManager", "Module installation canceled")
                            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_CANCELLED
                            onError?.invoke("Installation was cancelled")
                        }

                        InstallState.STATE_FAILED -> {
                            Log.i("QRScannerManager", "Module installation failed")
                            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_FAILED
                            onError?.invoke("Installation failed")
                        }

                        else -> {
                            Log.i(
                                "QRScannerManager",
                                "Module installation terminated with invalid state"
                            )
                            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_FAILED
                            onError?.invoke("Installation failed with unknown error")
                        }
                    }
                }
            }

            private fun isTerminateState(@InstallState state: Int): Boolean {
                return state == InstallState.STATE_CANCELED ||
                        state == InstallState.STATE_COMPLETED ||
                        state == InstallState.STATE_FAILED
            }
        }

        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(scanner!!)
            .setListener(currentInstallListener!!)
            .build()
        moduleInstallClient.installModules(moduleInstallRequest)
            .addOnSuccessListener {
                Log.i("QRScannerManager", "Module installation requested")
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Log.w(
                    "QRScannerManager",
                    "Module installation failed: ${exception.message}"
                )
                // Check if it's an API unavailable error
                if (exception.message?.contains("API_UNAVAILABLE") == true ||
                    exception.message?.contains("not available on this device") == true
                ) {
                    _moduleAvailabilityState.value = ModuleAvailability.API_UNAVAILABLE
                    onError?.invoke("QR Scanner is not available. Please update Google Play Services and try again.")
                } else {
                    _moduleAvailabilityState.value = ModuleAvailability.INSTALL_FAILED
                    onError?.invoke("Installation failed: ${exception.message}")
                }
            }
    }

    /**
     * Cancels ongoing installation
     */
    override fun cancelInstallation() {
        if (_moduleAvailabilityState.value == ModuleAvailability.INSTALLING) {
            Log.d("QRScannerManager", "Cancelling installation")
            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_CANCELLED
            currentInstallListener = null
            onProgressCallback = null
            onCompleteCallback = null
            onErrorCallback = null
        }
    }

    override fun getModuleAvailability() = _moduleAvailabilityState.value
}
