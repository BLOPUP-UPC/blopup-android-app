package edu.upc.openmrs.test.activities.addeditpatient

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.R
import edu.upc.openmrs.activities.addeditpatient.nationality.Nationality
import edu.upc.openmrs.activities.addeditpatient.nationality.NationalityAdapter
import io.mockk.*
import junit.framework.TestCase.assertEquals

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class NationalityAdapterTest {
    private lateinit var adapter: NationalityAdapter

    @Before
    internal fun setup() {
        val data = arrayOf(
            Nationality.ALBANIA,
            Nationality.ALGERIA,
            Nationality.ANDORRA,
            Nationality.ARGENTINA
        )
        adapter = NationalityAdapter(ApplicationProvider.getApplicationContext(), R.layout.item_nationality, data)
    }

    @Test
    fun testGetItem() {
        assertEquals("ALBANIA", (adapter.getItem(0)).name)
    }

    @Test
    fun `when search for Algeria then the list should only return one item`(){

        val expected = arrayOf(Nationality.ALGERIA)

        adapter.filter.filter("Algeria")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    @Test
    fun `when search for 'Al' then the list should return 2 items`(){

        val expected = arrayOf(
            Nationality.ALBANIA, Nationality.ALGERIA)

        adapter.filter.filter("Al")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    @Test
    fun `when search for 'z' then the list should be empty`(){

        val expected = emptyArray<Nationality>()

        adapter.filter.filter("z")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    @Test
    fun `when language is set to Spain should sort alphabetically by translated names`() {
        val data = arrayOf(Nationality.SOUTH_SUDAN, Nationality.SPAIN)

        val context: Context = getContextWithSpanishLocale()

        adapter = NationalityAdapter(context, R.layout.item_nationality, data)

        val expected = arrayOf(Nationality.SPAIN, Nationality.SOUTH_SUDAN)

        adapter.filter.filter("")

        val result = adapter.filteredList

        assert(expected.contentEquals(result))
    }

    private fun getContextWithSpanishLocale(): Context {
        val config = Configuration()
        config.setLocale(Locale("es", "ES"))
        val context: Context =
            ApplicationProvider.getApplicationContext<Context?>().createConfigurationContext(config)
        return context
    }
}
