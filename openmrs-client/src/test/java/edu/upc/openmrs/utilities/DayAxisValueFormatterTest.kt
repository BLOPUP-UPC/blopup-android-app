package edu.upc.openmrs.utilities

import com.github.mikephil.charting.components.AxisBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DayAxisValueFormatterTest{

    private val value1 : Float = 0.5F
    private val value2 : Float = 1.2F
    private val ax = object : AxisBase(){}

    @Test
    fun correctFormatInputString_returnsFormattedValue(){
        val result1 = DayAxisValueFormatter(arrayListOf("01/04/2020", "01/04/2021")).getFormattedValue(value1, ax)
        val result2 = DayAxisValueFormatter(arrayListOf("01/04/2020", "01/04/2021")).getFormattedValue(value2, ax)
        print(result1)
        print(result2)
        assertEquals("Apr 1, '20", result1)
        assertEquals("Apr 1, '21", result2)
    }

    @Test
    fun wrongFormatOfInputString_returnsIncorrectValue(){
        val result1 = DayAxisValueFormatter(arrayListOf("01-04-2020", "01-04-2021")).getFormattedValue(value1, ax)
        val result2 = DayAxisValueFormatter(arrayListOf("01-04-2020", "01-04-2021")).getFormattedValue(value2, ax)
        print(result1)
        print(result2)
        assertNotEquals("Apr 1, '20", result1)
        assertNotEquals("Apr 1, '21", result2)
    }

    @Test(expected = NullPointerException::class)
    fun invalidFormat_throwsNPE(){
        val result = DayAxisValueFormatter(arrayListOf("01042020", "01042021")).getFormattedValue(value1, ax)
        print(result)
    }
}