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

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.format.DateTimeFormatterBuilder

object DateUtils {
    private const val PARSE_DATE_FORMAT = "d/M/yyyy"
    private const val DATE_FORMAT = "dd/MM/yyyy"
    private const val DATE_WITH_TIME_FORMAT = "dd/MM/yyyy HH:mm"
    private const val OPEN_MRS_RESPONSE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    // Use this method to parse DateTime from the API response
    fun parseInstantFromOpenmrsDate(dateTime: String): Instant {
        val formatter = DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern(OPEN_MRS_RESPONSE_FORMAT))
            .appendOptional(ISO_DATE_TIME)
            .toFormatter()
        return LocalDateTime.parse(dateTime, formatter).toInstant(java.time.ZoneOffset.UTC)
    }

    // Use this method to parse the LocalDate from the API response
    fun parseLocalDateFromOpenmrsDate(dateTime: String): LocalDate {
        val instant = parseInstantFromOpenmrsDate(dateTime)
        // We need to assume that the date were stored in Madrid timezone
        // So we avoid day misleading due to timezone differences
        // 1988-04-08 is returned as 1988-04-07T22:00:00.000+0000
        return instant.atZone(ZoneId.of("Europe/Madrid")).toLocalDate()
    }

    fun parseLocalDateFromDefaultFormat(date: String): LocalDate {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(PARSE_DATE_FORMAT))
    }

    fun getEstimatedBirthdate(yearDiff: Int, today: LocalDate): LocalDate {
        return today.minusYears(yearDiff.toLong())
    }
    @JvmStatic
    fun LocalDate.formatToApiRequest(zoneId: ZoneId = ZoneId.systemDefault()): String {
        return this.atStartOfDay().atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern(OPEN_MRS_RESPONSE_FORMAT))
    }

    fun LocalDate.formatToDefaultFormat(): String {
        return this.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
    }

    @JvmStatic
    fun Instant.formatToDateAndTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
        return this.atZone(zoneId).toLocalDateTime().format(DateTimeFormatter.ofPattern(DATE_WITH_TIME_FORMAT))
    }

    fun Instant.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return this.atZone(zoneId).toLocalDate()
    }
}
