package com.user_425.ewallet_relay

import com.user_425.ewallet_relay.data.model.PackageFilter
import org.junit.Assert.*
import org.junit.Test

class NotificationFilterTest {

    @Test
    fun testLegacyMigrationParser() {
        val defaultFilters = listOf(
            PackageFilter("com.gojek.gopay", "GoPay"),
            PackageFilter("com.gojek.gopaymerchant", "GoPay Merchant"),
            PackageFilter("id.dana", "Dana"),
            PackageFilter("com.shopeepay.id", "ShopeePay"),
            PackageFilter("ovo.id", "OVO"),
            PackageFilter("com.telkom.mwallet", "LinkAja")
        )

        fun parseFilterPackages(raw: String): List<PackageFilter> {
            if (raw.isBlank()) {
                return defaultFilters
            }
            if (raw.trim().startsWith("[")) {
                return try {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<PackageFilter>>() {}.type
                    gson.fromJson<List<PackageFilter>>(raw, type) ?: defaultFilters
                } catch (e: Exception) {
                    defaultFilters
                }
            } else {
                return raw.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { pkg ->
                        val defaultMatch = defaultFilters.find { it.packageName.equals(pkg, ignoreCase = true) }
                        defaultMatch ?: PackageFilter(packageName = pkg, appName = pkg, containWords = "")
                    }
            }
        }

        // Test default parsing
        var result = parseFilterPackages("")
        assertEquals(6, result.size)
        assertEquals("GoPay", result[0].appName)

        // Test legacy string migration parsing
        result = parseFilterPackages("id.dana, com.whatsapp, com.gojek.gopay")
        assertEquals(3, result.size)
        assertEquals("Dana", result[0].appName)
        assertEquals("id.dana", result[0].packageName)
        assertEquals("com.whatsapp", result[1].appName)
        assertEquals("GoPay", result[2].appName)

        // Test JSON list parsing
        val json = """
            [
              {"packageName":"com.custom.app","appName":"Custom App","containWords":"success","enabled":false},
              {"packageName":"id.dana","appName":"Dana","containWords":"gopay,transfer"}
            ]
        """.trimIndent()
        result = parseFilterPackages(json)
        assertEquals(2, result.size)
        assertEquals("Custom App", result[0].appName)
        assertEquals("success", result[0].containWords)
        assertEquals(false, result[0].enabled)
        assertEquals("Dana", result[1].appName)
        assertEquals("gopay,transfer", result[1].containWords)
        assertEquals(true, result[1].enabled)
    }

    @Test
    fun testKeywordFiltering() {
        fun shouldForward(
            enabled: Boolean,
            containWords: String,
            title: String?,
            text: String?,
            subText: String?,
            bigText: String?
        ): Boolean {
            if (!enabled) {
                return false
            }
            val keywords = containWords.split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                
            if (keywords.isEmpty()) {
                return true
            }
            
            val content = listOfNotNull(title, text, subText, bigText)
                .joinToString(" ")
                .lowercase()
                
            return keywords.any { content.contains(it) }
        }

        // Test disabled filter
        assertFalse(shouldForward(false, "", "GoPay", "Transfer Sukses", null, null))
        assertFalse(shouldForward(false, "sukses", "GoPay", "Transfer Sukses", null, null))

        // Empty filter matches all when enabled
        assertTrue(shouldForward(true, "", "GoPay", "Transfer Sukses", null, null))
        assertTrue(shouldForward(true, "   ", "GoPay", "Transfer Sukses", null, null))

        // Exact match
        assertTrue(shouldForward(true, "Transfer", "GoPay", "Transfer Sukses", null, null))
        assertTrue(shouldForward(true, "sukses", "GoPay", "Transfer Sukses", null, null))

        // Case insensitivity
        assertTrue(shouldForward(true, "TRANSFER", "GoPay", "Transfer Sukses", null, null))
        assertTrue(shouldForward(true, "sukses", "GoPay", "TRANSFER SUKSES", null, null))

        // Multiple terms (split by comma)
        assertTrue(shouldForward(true, "gopay,ovo,dana", "GoPay", "Transfer", null, null))
        assertTrue(shouldForward(true, "gopay, ovo, dana", "Dana", "Transfer", null, null))
        assertFalse(shouldForward(true, "ovo, linkaja", "GoPay", "Transfer Sukses", null, null))

        // Partial word containment
        assertTrue(shouldForward(true, "pay", "GoPay", "Transfer", null, null))
    }
}
