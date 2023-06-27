package edu.upc.openmrs.test.activities.addeditpatient

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.R
import edu.upc.openmrs.activities.addeditpatient.Nationality
import edu.upc.openmrs.activities.addeditpatient.NationalityAdapter
import io.mockk.*
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NationalityAdapterTest {
    private lateinit var adapter: NationalityAdapter

    @Before
    internal fun setup() {
        val data = listOf(
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

        val expected = listOf(Nationality.ALGERIA)

        adapter.filter.filter("Algeria")

        val result = adapter.filteredList

        assertEquals(expected, result)
    }

    @Test
    fun `when search for 'Al' then the list should return 2 items`(){

        val expected = listOf(
            Nationality.ALBANIA, Nationality.ALGERIA)

        adapter.filter.filter("Al")

        val result = adapter.filteredList

        assertEquals(expected, result)
    }

    @Test
    fun `when search for 'z' then the list should be empty`(){

        val expected = emptyList<Nationality>()

        adapter.filter.filter("z")

        val result = adapter.filteredList

        assertEquals(expected, result)
    }
}
