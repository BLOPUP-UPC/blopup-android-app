package edu.upc.blopup.model

import edu.upc.R

enum class Gender {

    MALE {
        override fun relatedText() = R.string.male
        override fun value() = "M"
    },
    FEMALE {
        override fun relatedText() = R.string.female
        override fun value() = "F"
    },
    NON_BINARY {
        override fun relatedText() = R.string.non_binary
        override fun value() = "N"
    };

    abstract fun relatedText(): Int
    abstract fun value(): String

}