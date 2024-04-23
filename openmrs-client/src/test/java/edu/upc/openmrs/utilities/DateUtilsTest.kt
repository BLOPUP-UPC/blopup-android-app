package edu.upc.openmrs.utilities

import edu.upc.sdk.utilities.DateUtils.formatUsingLocale
import edu.upc.sdk.utilities.DateUtils.parseInstantFromOpenmrsDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class DateUtilsTest {
    @Test
    fun `create Instant from API format`() {
        val apiDate = "2024-04-22T07:25:03.000+0000"

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
}