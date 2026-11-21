package com.example.data.model

import android.net.Uri
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.NumberFormat
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class BotAccentTheme(val key: String) {
    EMERALD("EMERALD"),
    SAPPHIRE("SAPPHIRE"),
    GOLD("GOLD");

    companion object {
        fun fromKey(raw: String): BotAccentTheme =
            entries.firstOrNull { it.key == raw } ?: EMERALD
    }
}

enum class PresetImageKey(val key: String, val label: String) {
    PRAISE_SCALPER("praise_scalper", "Praise Scalper Pro (Green)"),
    GILBERT_SCALPER("gilbert_scalper", "Gilbert Scalper Pro (Blue)"),
    DUBAI_SKYLINE("dubai_skyline", "Dubai Skyscraper Night"),
    EA_MOBILE_CONNECT("ea_mobile_connect", "EA Mobile Connect"),
    CUSTOM("custom", "Custom Uploaded / URL");

    companion object {
        fun fromKey(raw: String): PresetImageKey =
            entries.firstOrNull { it.key == raw } ?: PRAISE_SCALPER
    }
}

enum class InquiryChannel(val key: String) {
    WHATSAPP_BUY("WHATSAPP_BUY"),
    WHATSAPP_CONTACT("WHATSAPP_CONTACT"),
    BROKER_VISIT("BROKER_VISIT");

    companion object {
        fun fromKey(raw: String): InquiryChannel =
            entries.firstOrNull { it.key == raw } ?: WHATSAPP_BUY
    }
}

@Keep
@IgnoreExtraProperties
data class TradingBot(
    @DocumentId val id: String = "",
    val name: String = "",
    val subtitle: String = "POWERED BY EA MOBILE CONNECT.",
    val description: String = "",
    val originalPriceZar: Long = 1500L,
    val promoPriceZar: Long = 200L,
    val whatsappDisplay: String = "",
    val whatsappE164: String = "",
    val buttonLabel: String = "",
    val accentTheme: String = BotAccentTheme.EMERALD.key,
    val presetImageKey: String = PresetImageKey.PRAISE_SCALPER.key,
    val imageUrl: String = "",
    val localImageName: String = "",
    val active: Boolean = true,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun buildWhatsAppPurchaseUrl(): String {
        val cleanNumber = whatsappE164.filter { it.isDigit() }
        val displayTitle = if (name == name.uppercase()) {
            name.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
            }
        } else {
            name
        }
        val message = "Hi, I want to purchase the $displayTitle for R$promoPriceZar. Please provide me with the purchasing details."
        return "https://wa.me/$cleanNumber?text=${Uri.encode(message)}"
    }

    fun buildWhatsAppContactUrl(): String {
        val cleanNumber = whatsappE164.filter { it.isDigit() }
        val message = "Hi PraiseTraders team, I would like more information about $name."
        return "https://wa.me/$cleanNumber?text=${Uri.encode(message)}"
    }
}

@Keep
@IgnoreExtraProperties
data class Announcement(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val badgeLabel: String = "UPDATE",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PromoOffer(
    @DocumentId val id: String = "",
    val title: String = "",
    val botName: String = "",
    val originalPriceZar: Long = 1500L,
    val promoPriceZar: Long = 200L,
    val tagline: String = "PREMIUM TRADING TECHNOLOGY. UNBELIEVABLE VALUE.",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SiteSettings(
    @DocumentId val id: String = "global",
    val recommendedBrokerUrl: String = "https://crm.razormarkets.co.za/links/go/14",
    val recommendedBrokerHeading: String = "TRADE WITH OUR RECOMMENDED BROKER.",
    val recommendedBrokerDescription: String = "Discover our recommended brokerage partner and explore its trading services.",
    val blackFridayHeading: String = "BLACK FRIDAY MEGA SALE",
    val blackFridaySlogan: String = "PREMIUM TRADING TECHNOLOGY. UNBELIEVABLE VALUE.",
    val blackFridayNotice: String = "BLACK FRIDAY SPECIAL AVAILABLE UNTIL FURTHER NOTICE.",
    val eaConnectHeading: String = "POWERED BY EA MOBILE CONNECT.",
    val eaConnectDescription: String = "Our trading solutions are powered by EA MOBILE CONNECT, bringing automated trading technology and connectivity to our trading products.",
    val aboutHeading: String = "WHO WE ARE",
    val aboutDescription: String = "PRAISETRADERS is an algorithmic trading technology brand dedicated to providing modern automated trading solutions. Through our premium trading products, powered by EA MOBILE CONNECT, we aim to make automated trading technology accessible to traders.",
    val riskDisclaimer: String = "Trading financial instruments involves substantial risk. Automated trading systems do not guarantee profits. Past performance does not guarantee future results. Customers should understand the risks before using automated trading software.",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserInquiry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val botName: String = "",
    val whatsappNumber: String = "",
    val channel: String = InquiryChannel.WHATSAPP_BUY.key,
    val priceZar: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

fun formatZar(amount: Long): String {
    val nf = NumberFormat.getIntegerInstance(Locale.US)
    return "R${nf.format(amount)}"
}

fun normalizeSouthAfricanWhatsApp(input: String): Pair<String, String> {
    val digits = input.filter { it.isDigit() }
    val e164 = when {
        digits.startsWith("27") && digits.length >= 11 -> digits
        digits.startsWith("0") && digits.length == 10 -> "27${digits.drop(1)}"
        else -> digits
    }
    val display = if (digits.startsWith("0") && digits.length == 10) {
        "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6, 10)}"
    } else if (e164.startsWith("27") && e164.length == 11) {
        val local = "0${e164.drop(2)}"
        "${local.substring(0, 3)} ${local.substring(3, 6)} ${local.substring(6, 10)}"
    } else {
        input.trim()
    }
    return display to e164
}

object DefaultCatalog {
    val praiseScalperPro = TradingBot(
        id = "praise_scalper_pro_v6",
        name = "PRAISE SCALPER PRO V6+ ENGINE",
        subtitle = "POWERED BY EA MOBILE CONNECT.",
        description = "Flagship automated algorithmic scalping Expert Advisor engineered for precision market execution, streamlined mobile connectivity, and modern portfolio workflows.",
        originalPriceZar = 1500L,
        promoPriceZar = 200L,
        whatsappDisplay = "069 224 7990",
        whatsappE164 = "27692247990",
        buttonLabel = "BUY PRAISE SCALPER PRO",
        accentTheme = BotAccentTheme.EMERALD.key,
        presetImageKey = PresetImageKey.PRAISE_SCALPER.key,
        active = true,
        sortOrder = 0L
    )

    val gilbertScalperPro = TradingBot(
        id = "gilbert_scalper_pro_v6",
        name = "GILBERT SCALPER PRO V6+ ENGINE",
        subtitle = "POWERED BY EA MOBILE CONNECT.",
        description = "High-precision sapphire series automated trading Expert Advisor built for adaptive market execution and seamless connectivity via EA MOBILE CONNECT.",
        originalPriceZar = 1500L,
        promoPriceZar = 200L,
        whatsappDisplay = "066 391 1218",
        whatsappE164 = "27663911218",
        buttonLabel = "BUY GILBERT SCALPER PRO",
        accentTheme = BotAccentTheme.SAPPHIRE.key,
        presetImageKey = PresetImageKey.GILBERT_SCALPER.key,
        active = true,
        sortOrder = 1L
    )

    val defaultBots = listOf(praiseScalperPro, gilbertScalperPro)

    val defaultSiteSettings = SiteSettings()

    val defaultPromoOffers = listOf(
        PromoOffer(
            id = "offer_praise_bf",
            title = "BLACK FRIDAY SPECIAL",
            botName = "PRAISE SCALPER PRO V6+ ENGINE",
            originalPriceZar = 1500L,
            promoPriceZar = 200L,
            tagline = "PREMIUM TRADING TECHNOLOGY. UNBELIEVABLE VALUE.",
            active = true
        ),
        PromoOffer(
            id = "offer_gilbert_bf",
            title = "BLACK FRIDAY SPECIAL",
            botName = "GILBERT SCALPER PRO V6+ ENGINE",
            originalPriceZar = 1500L,
            promoPriceZar = 200L,
            tagline = "PREMIUM TRADING TECHNOLOGY. UNBELIEVABLE VALUE.",
            active = true
        )
    )

    val defaultAnnouncements = listOf(
        Announcement(
            id = "ann_black_friday_live",
            title = "BLACK FRIDAY MEGA SALE NOW LIVE",
            message = "Both PRAISE SCALPER PRO V6+ ENGINE and GILBERT SCALPER PRO V6+ ENGINE are available for R200 until further notice.",
            badgeLabel = "BLACK FRIDAY",
            active = true
        )
    )
}
