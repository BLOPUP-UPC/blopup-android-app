package com.openmrs.android_sdk.library.models

import java.io.Serializable

class Diagnosis constructor() : Resource(), Serializable {
    constructor(display: String) : this() {
        this.display = display
    }

    constructor(uuid: String, display: String, links: List<Link>, id: Long) : this() {
        this.uuid = uuid
        this.display = display
        this.links = links
        this.id = id
    }
}