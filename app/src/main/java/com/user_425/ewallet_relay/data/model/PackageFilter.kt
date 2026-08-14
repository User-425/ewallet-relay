package com.user_425.ewallet_relay.data.model

import com.google.gson.annotations.SerializedName

data class PackageFilter(
    @SerializedName("packageName")
    val packageName: String,
    @SerializedName("appName")
    val appName: String,
    @SerializedName("containWords")
    val containWords: String = "",
    @SerializedName("enabled")
    private val _enabled: Boolean? = true
) {
    val enabled: Boolean
        get() = _enabled ?: true

    constructor(packageName: String, appName: String, containWords: String = "", enabled: Boolean) :
        this(packageName, appName, containWords, _enabled = enabled)

    fun copy(
        packageName: String = this.packageName,
        appName: String = this.appName,
        containWords: String = this.containWords,
        enabled: Boolean = this.enabled
    ): PackageFilter = PackageFilter(packageName, appName, containWords, enabled)
}
