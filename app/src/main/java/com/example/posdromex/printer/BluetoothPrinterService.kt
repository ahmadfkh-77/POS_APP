package com.example.posdromex.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

/**
 * Bluetooth ESC/POS Printer Service for 58mm thermal printers
 * Uses Bluetooth Classic (RFCOMM/SPP) protocol
 */
class BluetoothPrinterService(private val context: Context) {

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var connectedDevice: BluetoothDevice? = null

    companion object {
        // Standard SPP UUID for Bluetooth serial communication
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        // ESC/POS Commands for 58mm printer (32 characters per line)
        const val CHARS_PER_LINE = 32

        // Initialize printer
        val ESC_INIT = byteArrayOf(0x1B, 0x40)

        // Text alignment
        val ESC_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
        val ESC_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        val ESC_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)

        // Text style
        val ESC_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        val ESC_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
        val ESC_DOUBLE_HEIGHT_ON = byteArrayOf(0x1B, 0x21, 0x10)
        val ESC_DOUBLE_WIDTH_ON = byteArrayOf(0x1B, 0x21, 0x20)
        val ESC_DOUBLE_SIZE_ON = byteArrayOf(0x1B, 0x21, 0x30)
        val ESC_NORMAL_SIZE = byteArrayOf(0x1B, 0x21, 0x00)

        // Line feed and cut
        val LF = byteArrayOf(0x0A)
        val ESC_FEED_LINES = byteArrayOf(0x1B, 0x64, 0x04) // Feed 4 lines
        val ESC_CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x00) // Full cut
        val ESC_PARTIAL_CUT = byteArrayOf(0x1D, 0x56, 0x01) // Partial cut
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter
    }

    /**
     * Check if Bluetooth is available and enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Get list of paired Bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    /**
     * Connect to a Bluetooth printer by MAC address
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(macAddress: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect()

            val device = bluetoothAdapter?.getRemoteDevice(macAddress)
                ?: return@withContext Result.failure(Exception("Device not found"))

            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            connectedDevice = device

            // Initialize printer
            outputStream?.write(ESC_INIT)

            Result.success(Unit)
        } catch (e: IOException) {
            disconnect()
            Result.failure(Exception("Connection failed: ${e.message}"))
        }
    }

    /**
     * Connect to a Bluetooth device directly
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect()

            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            connectedDevice = device

            // Initialize printer
            outputStream?.write(ESC_INIT)

            Result.success(Unit)
        } catch (e: IOException) {
            disconnect()
            Result.failure(Exception("Connection failed: ${e.message}"))
        }
    }

    /**
     * Disconnect from the printer
     */
    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            // Ignore close errors
        } finally {
            outputStream = null
            bluetoothSocket = null
            connectedDevice = null
        }
    }

    /**
     * Check if printer is connected
     */
    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    /**
     * Get currently connected device
     */
    fun getConnectedDevice(): BluetoothDevice? = connectedDevice

    /**
     * Print raw bytes
     */
    suspend fun printRaw(data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            outputStream?.write(data) ?: return@withContext Result.failure(Exception("Not connected"))
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(Exception("Print failed: ${e.message}"))
        }
    }

    /**
     * Print text with optional styling
     */
    suspend fun printText(
        text: String,
        bold: Boolean = false,
        centered: Boolean = false,
        doubleSize: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val os = outputStream ?: return@withContext Result.failure(Exception("Not connected"))

            // Set alignment
            if (centered) os.write(ESC_ALIGN_CENTER) else os.write(ESC_ALIGN_LEFT)

            // Set style
            if (doubleSize) os.write(ESC_DOUBLE_SIZE_ON)
            if (bold && !doubleSize) os.write(ESC_BOLD_ON)

            // Print text
            os.write(text.toByteArray(Charsets.UTF_8))
            os.write(LF)

            // Reset style
            os.write(ESC_NORMAL_SIZE)
            os.write(ESC_BOLD_OFF)
            os.write(ESC_ALIGN_LEFT)

            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(Exception("Print failed: ${e.message}"))
        }
    }

    /**
     * Print a line (dashes)
     */
    suspend fun printLine(): Result<Unit> {
        return printText("-".repeat(CHARS_PER_LINE))
    }

    /**
     * Print empty lines
     */
    suspend fun feedLines(lines: Int = 1): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val os = outputStream ?: return@withContext Result.failure(Exception("Not connected"))
            repeat(lines) {
                os.write(LF)
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(Exception("Print failed: ${e.message}"))
        }
    }

    /**
     * Feed paper and cut
     */
    suspend fun feedAndCut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val os = outputStream ?: return@withContext Result.failure(Exception("Not connected"))
            os.write(ESC_FEED_LINES)
            os.write(ESC_PARTIAL_CUT)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(Exception("Print failed: ${e.message}"))
        }
    }

    /**
     * Print a formatted line with left and right text
     */
    suspend fun printTwoColumns(left: String, right: String): Result<Unit> {
        val availableSpace = CHARS_PER_LINE - right.length
        val leftTruncated = if (left.length > availableSpace - 1) {
            left.take(availableSpace - 1)
        } else {
            left
        }
        val padding = " ".repeat(CHARS_PER_LINE - leftTruncated.length - right.length)
        return printText(leftTruncated + padding + right)
    }

    /**
     * Print a test page
     */
    suspend fun printTestPage(): Result<Unit> {
        val results = mutableListOf<Result<Unit>>()

        results.add(printRaw(ESC_INIT))
        results.add(printText("POSDromex", bold = true, centered = true, doubleSize = true))
        results.add(printText("Test Print", centered = true))
        results.add(printLine())
        results.add(printText("Printer is working!"))
        results.add(printText("58mm ESC/POS Ready"))
        results.add(printLine())
        results.add(printTwoColumns("Left", "Right"))
        results.add(printTwoColumns("Item Name", "$10.00"))
        results.add(printLine())
        results.add(feedLines(3))

        return results.lastOrNull { it.isFailure } ?: Result.success(Unit)
    }
}
