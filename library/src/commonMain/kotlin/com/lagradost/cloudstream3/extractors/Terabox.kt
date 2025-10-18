package com.lagradost.cloudstream3.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*

open class Terabox : ExtractorApi() {
    private val apiUrl: String = "https://terabox-pro-api.vercel.app/api?link="
    private val newApi: String = "https://api.0xcloud.workers.dev"
    override val name: String = "Terabox"
    override val mainUrl: String = "https://www.terabox.com"
    override val requiresReferer: Boolean = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val links = mutableListOf<ExtractorLink>()
        val finalUrl = if (url.startsWith("http")) url else "$mainUrl/$url"

        try {
            val jsonResponse = app.get("$apiUrl$finalUrl").text
            val responseData: List<TeraboxResponse>? = tryParseJson(jsonResponse)

            responseData?.forEach { item: TeraboxResponse ->
                links.add(
                    newExtractorLink(name, name, item.file) {
                        this.referer = referer ?: mainUrl
                        this.quality = getQualityFromName(item.label ?: "")
                    }
                )
            }

            if (links.isEmpty()) {
                links.add(
                    newExtractorLink(name, name, finalUrl) {
                        this.referer = referer ?: mainUrl
                        this.quality = Qualities.Unknown.value
                    }
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            links.add(
                newExtractorLink(name, name, finalUrl) {
                    this.referer = referer ?: mainUrl
                    this.quality = Qualities.Unknown.value
                }
            )
        }

        return links
    }

    private data class TeraboxResponse(
        @JsonProperty("file") val file: String,
        @JsonProperty("label") val label: String?
    )
}

class Terabox1024 : Terabox() {
    override val name: String = "Terabox1024"
    override val mainUrl: String = "https://www.1024terabox.com"
}

class Teraboxapp : Terabox() {
    override val name: String = "Teraboxapp"
    override val mainUrl: String = "https://terabox.app"
}

class Teraboxdm : Terabox() {
    override val name: String = "Teraboxdm"
    override val mainUrl: String = "https://dm.terabox.com"
}

class Teraboxdmapp : Terabox() {
    override val name: String = "Teraboxdmapp"
    override val mainUrl: String = "https://dm.terabox.app"
}
