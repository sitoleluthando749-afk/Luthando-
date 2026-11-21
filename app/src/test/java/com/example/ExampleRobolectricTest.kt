package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DefaultCatalog
import com.example.data.model.formatZar
import com.example.data.model.normalizeSouthAfricanWhatsApp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches PraiseTraders`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PraiseTraders", appName)
    }

    @Test
    fun `praise scalper pro whatsapp purchase url matches official specification`() {
        val expected = "https://wa.me/27692247990?text=Hi%2C%20I%20want%20to%20purchase%20the%20Praise%20Scalper%20Pro%20V6%2B%20Engine%20for%20R200.%20Please%20provide%20me%20with%20the%20purchasing%20details."
        assertEquals(expected, DefaultCatalog.praiseScalperPro.buildWhatsAppPurchaseUrl())
    }

    @Test
    fun `gilbert scalper pro whatsapp purchase url matches official specification`() {
        val expected = "https://wa.me/27663911218?text=Hi%2C%20I%20want%20to%20purchase%20the%20Gilbert%20Scalper%20Pro%20V6%2B%20Engine%20for%20R200.%20Please%20provide%20me%20with%20the%20purchasing%20details."
        assertEquals(expected, DefaultCatalog.gilbertScalperPro.buildWhatsAppPurchaseUrl())
    }

    @Test
    fun `recommended broker url and pricing format match specification`() {
        assertEquals(
            "https://crm.razormarkets.co.za/links/go/14",
            DefaultCatalog.defaultSiteSettings.recommendedBrokerUrl
        )
        assertEquals("R1,500", formatZar(1500L))
        assertEquals("R200", formatZar(200L))
        assertEquals("069 224 7990" to "27692247990", normalizeSouthAfricanWhatsApp("0692247990"))
    }
}
