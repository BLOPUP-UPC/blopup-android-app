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
            Nationality("Albania", R.drawable.flag_albania),
            Nationality("Algeria", R.drawable.flag_algeria),
            Nationality("Andorra", R.drawable.flag_andorra),
            Nationality("Argentina", R.drawable.flag_argentina)
        )
        adapter = NationalityAdapter(ApplicationProvider.getApplicationContext(), R.layout.item_nationality, data)
    }

    @Test
    fun testGetItem() {
        assertEquals("Albania", (adapter.getItem(0)).name)
    }

    @Test
    fun `when search for Algeria then the list should only return one item`(){

        val expected = listOf(Nationality("Algeria", R.drawable.flag_algeria))

        adapter.filter.filter("Algeria")

        val result = adapter.filteredList

        assertEquals(expected, result)
    }

    @Test
    fun `when search for 'Al' then the list should return 2 items`(){

        val expected = listOf(
            Nationality("Albania", R.drawable.flag_albania),
            Nationality("Algeria", R.drawable.flag_algeria))

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
