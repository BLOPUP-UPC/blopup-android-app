package edu.upc.openmrs.test.activities.addeditpatient

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.R
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.Country
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.CountryAdapter
import junit.framework.TestCase.assertEquals

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class CountryAdapterTest {
    private lateinit var adapter: CountryAdapter

    @Before
    internal fun setup() {
        val data = arrayOf(
            Country.ALBANIA,
            Country.ALGERIA,
            Country.ANDORRA,
            Country.ARGENTINA
        )
        adapter = CountryAdapter(ApplicationProvider.getApplicationContext(), R.layout.item_country, data)
    }

    @Test
    fun testGetItem() {
        assertEquals("ALBANIA", (adapter.getItem(0)).name)
    }

    @Test
    fun `when search for Algeria then the list should only return one item`(){

        val expected = arrayOf(Country.ALGERIA)

        adapter.filter.filter("Algeria")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    @Test
    fun `when search for 'Al' then the list should return 2 items`(){

        val expected = arrayOf(
            Country.ALBANIA, Country.ALGERIA)

        adapter.filter.filter("Al")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    @Test
    fun `when search for 'z' then the list should be empty`(){

        val expected = emptyArray<Country>()

        adapter.filter.filter("z")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    @Test
    fun `when language is set to Spain should sort alphabetically by translated names`() {
        val data = arrayOf(Country.SOUTH_SUDAN, Country.SPAIN)

        val context: Context = getContextWithSpanishLocale()

        adapter = CountryAdapter(context, R.layout.item_country, data)

        val expected = arrayOf(Country.SPAIN, Country.SOUTH_SUDAN)

        adapter.filter.filter("")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    private fun getContextWithSpanishLocale(): Context {
        val config = Configuration()
        config.setLocale(Locale("es", "ES"))
        return ApplicationProvider.getApplicationContext<Context?>()
            .createConfigurationContext(config)
    }
}
