/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.sdk.utilities

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import kotlin.math.abs

@Parcelize
class InputField(@JvmField var concept: String?) : Serializable, Parcelable {
    @IgnoredOnParcel
    @JvmField
    var id: Int

    @IgnoredOnParcel
    @JvmField
    var value = -1.0

    @IgnoredOnParcel
    @JvmField
    var isRed = false

    init {
        id = abs(concept.hashCode())
    }

    constructor(parcel: Parcel) : this(parcel.readString()) {
        id = parcel.readInt()
        value = parcel.readDouble()
        isRed = parcel.readInt() == 1
    }
}