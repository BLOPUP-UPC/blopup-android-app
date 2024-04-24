package edu.upc.openmrs.utilities

import edu.upc.sdk.utilities.DateUtils.formatUsingLocale
import edu.upc.sdk.utilities.DateUtils.getDateTimeFromDifference
import edu.upc.sdk.utilities.DateUtils.parseInstantFromOpenmrsDate
import edu.upc.sdk.utilities.DateUtils.parseLocalDateFromDefaultFormat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

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
        val apiDate = "2024-04-23T07:25:03.638+0000"
        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals("2024-04-23T07:25:03.638Z", instant.toString())
    }

    @Test
    fun `format Instant to API format without millisecs`() {
        val apiDate = "2024-04-23T07:25:03.000+0000"
        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals("2024-04-23T07:25:03Z", instant.toString())
    }

    @Test
    fun `format Instant to Madrid locale and timezone`() {
        val apiDate = "2024-04-23T07:25:03.000+0000"
        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals(
            "23/4/24, 9:25",
            instant.formatUsingLocale(Locale("es", "ES"))
        )
    }

    @Test
    fun `convert instant to LocalDate`() {
        val apiDate = "2024-04-23T07:25:03.000+0000"
        val instant = parseInstantFromOpenmrsDate(apiDate)

        assertEquals(
            "23/4/24, 9:25",
            instant.formatUsingLocale(Locale("es", "ES"))
        )
    }

    @Test
    fun `get estimated day of birthday`() {
        val estimatedYears = 30
        val today = LocalDate.of(2024, 4, 15)
        val expectedBirthday = LocalDate.of(1994, 4, 15)

        assertEquals(
            expectedBirthday,
            getDateTimeFromDifference(estimatedYears, today)
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
}