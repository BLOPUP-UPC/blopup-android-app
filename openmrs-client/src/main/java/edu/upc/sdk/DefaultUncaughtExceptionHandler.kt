package edu.upc.sdk

import edu.upc.sdk.library.OpenMRSLogger

class DefaultUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        val logger = OpenMRSLogger()
        logger.e("${e.message} caused by ${e.cause}: ${e.message} caused by ${e.cause?.cause}", e)
    }
}
