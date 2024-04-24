/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.sdk.utilities

import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.format.DateTimeFormatterBuilder
import java.util.Date
import java.util.Locale

object DateUtils {
    const val DEFAULT_DATE_FORMAT = "dd/MM/yyyy"
    const val OPEN_MRS_RESPONSE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    fun parseInstantFromOpenmrsDate(dateTime: String): Instant {
        val formatter = DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern(OPEN_MRS_RESPONSE_FORMAT))
            .appendOptional(ISO_DATE_TIME)
            .toFormatter()
        return LocalDateTime.parse(dateTime, formatter).toInstant(java.time.ZoneOffset.UTC)
    }

    fun parseLocalDateFromOpenmrsDate(dateTime: String): LocalDate {
        val instant = parseInstantFromOpenmrsDate(dateTime)
        // We need to assume that the date were stored in Madrid timezone
        // So we avoid day misleading due to timezone differences
        // 1988-04-08 is returned as 1988-04-07T22:00:00.000+0000
        return instant.atZone(ZoneId.of("Europe/Madrid")).toLocalDate()
    }

    @JvmStatic
    fun Instant.formatUsingLocale(locale: Locale): String {
        val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale)
        return df.format(Date.from(this))
    }

    fun parseLocalDateFromDefaultFormat(date: String): LocalDate {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT))
    }

    fun getDateTimeFromDifference(yearDiff: Int, today: LocalDate): LocalDate {
        return today.minusYears(yearDiff.toLong())
    }

    fun LocalDate.formatToApiRequest(): String {
        return this.atStartOfDay().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern(OPEN_MRS_RESPONSE_FORMAT))
    }

    fun LocalDate.formatToDefaultFormat(): String {
        return this.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT))
    }

    fun Instant.toLocalDate(): LocalDate {
        return this.atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
