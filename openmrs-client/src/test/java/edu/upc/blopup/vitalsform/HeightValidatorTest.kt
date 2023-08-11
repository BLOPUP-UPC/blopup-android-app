package edu.upc.blopup.vitalsform

import junit.framework.TestCase.assertFalse
import org.junit.Test

class HeightValidatorTest {

    @Test
    fun `when height is between 50 and 280, form is valid`(){

        val result = HeightValidator.isValid("160", "", "", "", "")

        assert(result)
    }

    @Test
    fun `when height is out the limit 50 to 280, form is not valid`(){

        val result = HeightValidator.isValid("10", "", "", "", "")

        assertFalse(result)
    }

    @Test
    fun `when no height is passed but there is another vital value form is valid`(){

        val result = HeightValidator.isValid("", " 20", "", "", "")

        assert(result)
    }

    @Test
    fun `when height is out of limits and there is another vital value form is not valid`(){

        val result = HeightValidator.isValid("10", " 20", "", "", "")

        assertFalse(result)
    }
}





//class BMICalculatorTest(){
//
//    private val calculator = BMICalculator();
//
//    private val weightObservation = Observation().apply {
//        display = "Weight (kg): 70.0"
//        displayValue = "70.0"
//    }
//    private val heightObservation = Observation().apply {
//        display= "Height (cm): 165.0"
//        displayValue= "165.0"
//    }
//    @Test
//    fun givenAWeightAndHeight_calculateBMI(){
//        val list = listOf(weightObservation, heightObservation)
//
//        val result = calculator.execute(list)
//
//        Assert.assertEquals("25.7", result)
//    }