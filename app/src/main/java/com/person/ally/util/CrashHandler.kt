package com.person.ally.util

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashHandler {
    private const val CRASH_LOG_FILE = "crash_log.txt"
    private const val MAX_LOG_SIZE = 100 * 1024

    fun saveCrashLog(context: Context, throwable: Throwable) {
        try {
            val crashLog = buildCrashLog(throwable)
            val file = File(context.filesDir, CRASH_LOG_FILE)
            if (file.length() > MAX_LOG_SIZE) {
                file.delete()
            }
            file.appendText(crashLog)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCrashLog(context: Context): String? {
        return try {
            val file = File(context.filesDir, CRASH_LOG_FILE)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun clearCrashLog(context: Context) {
        try {
            val file = File(context.filesDir, CRASH_LOG_FILE)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun buildCrashLog(throwable: Throwable): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            .format(Date())
        val stackTrace = getStackTraceString(throwable)

        return buildString {
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine("CRASH REPORT")
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine("Timestamp: $timestamp")
            appendLine("Exception: ${throwable.javaClass.name}")
            appendLine("Message: ${throwable.message ?: "No message"}")
            appendLine("───────────────────────────────────────────────────────────────")
            appendLine("Stack Trace:")
            appendLine(stackTrace)
            appendLine()
        }
    }

    fun buildDeviceInfo(context: Context): String {
        return buildString {
            appendLine("Device Information:")
            appendLine("───────────────────────────────────────────────────────────────")
            appendLine("Manufacturer: ${android.os.Build.MANUFACTURER}")
            appendLine("Model: ${android.os.Build.MODEL}")
            appendLine("Android Version: ${android.os.Build.VERSION.RELEASE}")
            appendLine("SDK Level: ${android.os.Build.VERSION.SDK_INT}")
            appendLine("App Version: ${getAppVersion(context)}")
            appendLine()
        }
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
