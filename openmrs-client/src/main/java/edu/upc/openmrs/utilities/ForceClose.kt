package edu.upc.openmrs.utilities

import android.os.Build
import android.os.Process
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.OpenmrsAndroid
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class ForceClose : Thread.UncaughtExceptionHandler {
    companion object {
        private const val LINE_SEPARATOR = "\n"
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))
        val errorReport = StringBuilder()
        errorReport.append("************ CAUSE OF ERROR ************\n\n")
        errorReport.append(stackTrace.toString())
        errorReport.append("\n************ DEVICE INFORMATION ***********\n")
        errorReport.append("Brand: ")
        errorReport.append(Build.BRAND)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Device: ")
        errorReport.append(Build.DEVICE)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Model: ")
        errorReport.append(Build.MODEL)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Id: ")
        errorReport.append(Build.ID)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Product: ")
        errorReport.append(Build.PRODUCT)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("\n************ FIRMWARE ************\n")
        errorReport.append("SDK: ")
        errorReport.append(Build.VERSION.SDK_INT)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Release: ")
        errorReport.append(Build.VERSION.RELEASE)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("Incremental: ")
        errorReport.append(Build.VERSION.INCREMENTAL)
        errorReport.append(LINE_SEPARATOR)
        errorReport.append("\n************ APP LOGS ************\n")
        errorReport.append(getLogs())
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    private fun getLogs(): String? {
        val mOpenMRSLogger = OpenMRSLogger()
        var textLogs: String? = ""
        val filename = "${OpenmrsAndroid.getOpenMRSDir()}${File.separator}${mOpenMRSLogger.logFilename}"
        try {
            val myFile = File(filename)
            val fIn = FileInputStream(myFile)
            val myReader = BufferedReader(InputStreamReader(fIn))
            var aDataRow: String?
            while (myReader.readLine().also { aDataRow = it } != null) {
                textLogs += aDataRow
            }
            myReader.close()
        }  catch (e: Exception) {
            when (e) {
                is FileNotFoundException, is IOException -> {
                    e.printStackTrace()
                }
            }
        }
        return textLogs
    }
}