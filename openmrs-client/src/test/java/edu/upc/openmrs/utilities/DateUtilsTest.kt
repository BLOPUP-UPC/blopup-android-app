package edu.upc.openmrs.utilities

import edu.upc.sdk.utilities.DateUtils.formatToApiRequest
import edu.upc.sdk.utilities.DateUtils.formatToDateAndTime
import edu.upc.sdk.utilities.DateUtils.getEstimatedBirthdate
import edu.upc.sdk.utilities.DateUtils.parseInstantFromOpenmrsDate
import edu.upc.sdk.utilities.DateUtils.parseLocalDateFromDefaultFormat
import edu.upc.sdk.utilities.DateUtils.parseLocalDateFromOpenmrsDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsTest {
    @Test
    fun `create Instant from API format`() {
        val apiDate = "2024-04-22T07:25:03.000+0000"

        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals("2024-04-22T07:25:03Z", instant.toString())
    }

    @Test
    fun `create Instant from API format more tolerant`() {
        val apiDate = "2024-04-22T07:25:03Z"

        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals("2024-04-22T07:25:03Z", instant.toString())
    }

    @Test
    fun `format Instant to API format`() {
        val apiDate = "2024-04-23T07:25:03.638Z"
        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals("2024-04-23T07:25:03.638+0000", instant.formatToApiRequest())
    }

    @Test
    fun `format Instant to API format without millisecs`() {
        val apiDate = "2024-04-23T07:25:03.000+0000"
        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals("2024-04-23T07:25:03.000+0000", instant.formatToApiRequest())
    }

    @Test
    fun `format Instant to Madrid locale and timezone`() {
        val apiDate = "2024-04-23T07:25:03.000+0000"
        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals(
            "23/04/2024 09:25",
            instant.formatToDateAndTime(ZoneId.of("Europe/Madrid"))
        )
    }

    @Test
    fun `get estimated day of birthday`() {
        val estimatedYears = 30
        val today = LocalDate.of(2024, 4, 15)
        val expectedBirthday = LocalDate.of(1994, 4, 15)

        assertEquals(
            expectedBirthday,
            getEstimatedBirthdate(estimatedYears, today)
        )
    }

    @Test
    fun `LocalDate is formatted as OpenMRS expects`() {
        val today = LocalDate.of(2024, 4, 15)

        assertEquals(
            "2024-04-15",
            today.toString()
        )
    }

    @Test
    fun `LocalDate is parsed from default format`() {
        val date = "15/12/1980"
        val expectedLocalDate = LocalDate.of(1980, 12, 15)

        assertEquals(
            expectedLocalDate,
            parseLocalDateFromDefaultFormat(date)
        )
    }

    @Test
    fun `format birthday as localdate without timezone`() {
        val apiDate = "1988-04-07T22:00:00.000+0000"
        val expectedLocalDate = LocalDate.of(1988, 4, 8)

        assertEquals(
            expectedLocalDate,
            parseLocalDateFromOpenmrsDate(apiDate)
        )
    }

    @Test
    fun `format birthday as localdate from form`() {
        val formText = "1/4/2000"
        val expectedLocalDate = LocalDate.of(2000, 4, 1)

        assertEquals(
            expectedLocalDate,
            parseLocalDateFromDefaultFormat(formText)
        )
    }

    @Test
    fun `format birthday as localdate from form 2`() {
        val formText = "14/11/2012"
        val expectedLocalDate = LocalDate.of(2012, 11, 14)

        assertEquals(
            expectedLocalDate,
            parseLocalDateFromDefaultFormat(formText)
        )
    }

    @Test
    fun `format LocalDate to API format`() {
        val date = LocalDate.of(2024, 4, 8)
        val expectedApiDate = "2024-04-07T22:00:00.000+0000"

        assertEquals(expectedApiDate, date.formatToApiRequest(ZoneId.of("Europe/Madrid")))
    }
}