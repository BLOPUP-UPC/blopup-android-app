package edu.upc.openmrs.activities

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    val isActive: Boolean get() = isAdded
}
