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
import edu.upc.sdk.library.models.Answer
import java.io.Serializable

class SelectOneField : Serializable, Parcelable {
    var concept: String? = null
    var chosenAnswer: Answer? = null
    private var answerList: List<Answer?>

    constructor(answerList: List<Answer?>, concept: String?) {
        this.answerList = answerList
        this.concept = concept
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(concept)
        dest.writeSerializable(chosenAnswer)
        dest.writeList(answerList)
    }

    protected constructor(`in`: Parcel) {
        concept = `in`.readString()
        chosenAnswer = `in`.readSerializable() as Answer
        answerList = ArrayList()
        `in`.readList(answerList, Answer::class.java.classLoader)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SelectOneField?> = object : Parcelable.Creator<SelectOneField?> {
            override fun createFromParcel(source: Parcel): SelectOneField {
                return SelectOneField(source)
            }

            override fun newArray(size: Int): Array<SelectOneField?> {
                return arrayOfNulls(size)
            }
        }
    }
}