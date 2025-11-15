package com.autobill.billsmart.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class StoreLogo(
    @Column(name = "store_logo_url")
    var logoUrl: String = "",

    @Column(name = "store_logo_media_type")
    var mediaType: String = ""
)
