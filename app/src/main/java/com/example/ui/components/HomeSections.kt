package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BotAccentTheme
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.formatZar
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.SapphireGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

@Composable
fun PraiseTradersBrandMark(
    modifier: Modifier = Modifier,
    subtitle: String = "ALGORITHMIC TRADING SUITE"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.praisetraders_icon),
            contentDescription = "PraiseTraders Crest",
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.75f), RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        Column {
            Text(
                text = "PRAISETRADERS",
                style = MaterialTheme.typography.titleLarge,
                color = GoldBright,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(EmeraldGlow)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldGlow
                )
            }
        }
    }
}

@Composable
fun PraiseTradersHeaderBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ObsidianSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    EmeraldPrimary.copy(alpha = 0.65f),
                    GoldAccent.copy(alpha = 0.75f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            EmeraldDeep.copy(alpha = 0.32f),
                            ObsidianSurface,
                            ObsidianBlack.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PraiseTradersBrandMark(subtitle = "DUBAI FINTECH • EA MOBILE CONNECT")
            Surface(
                color = EmeraldDeep.copy(alpha = 0.9f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.7f))
            ) {
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldGlow,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun remainingSecondsInDay(): Long {
    val now = Calendar.getInstance()
    val endOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return ((endOfDay.timeInMillis - now.timeInMillis) / 1000L).coerceAtLeast(1L)
}

@Composable
fun BlackFridayTopPromoBanner(
    siteSettings: SiteSettings,
    onClaimOffer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember { mutableLongStateOf(remainingSecondsInDay()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            secondsLeft = remainingSecondsInDay()
        }
    }

    val hours = (secondsLeft / 3600L).toInt()
    val minutes = ((secondsLeft % 3600L) / 60L).toInt()
    val seconds = (secondsLeft % 60L).toInt()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("black_friday_top_promo_banner"),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(GoldBright, EmeraldPrimary, GoldAccent)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GoldDeep.copy(alpha = 0.72f),
                            ObsidianCard.copy(alpha = 0.96f),
                            EmeraldDeep.copy(alpha = 0.68f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GoldAccent,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = ObsidianBlack,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "BLACK FRIDAY DEALS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ObsidianBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatZar(1500L),
                        style = MaterialTheme.typography.labelMedium.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = TextMuted
                    )
                    Surface(
                        color = EmeraldDeep,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = formatZar(200L),
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldGlow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = siteSettings.blackFridayHeading,
                style = MaterialTheme.typography.headlineLarge,
                color = GoldBright,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${siteSettings.blackFridaySlogan} Both flagship EAs at R200.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Countdown Timer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownUnitTile(
                    value = String.format(Locale.US, "%02d", hours),
                    unit = "HRS",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GoldBright
                )
                CountdownUnitTile(
                    value = String.format(Locale.US, "%02d", minutes),
                    unit = "MIN",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GoldBright
                )
                CountdownUnitTile(
                    value = String.format(Locale.US, "%02d", seconds),
                    unit = "SEC",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClaimOffer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("claim_black_friday_offer_button"),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldBright,
                    contentColor = ObsidianBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CLAIM OFFER",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CountdownUnitTile(
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ObsidianBlack.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = EmeraldGlow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = GoldBright
            )
        }
    }
}

@Composable
fun EaMobileConnectSectionCard(
    siteSettings: SiteSettings,
    modifier: Modifier = Modifier
) {
    GlassFintechCard(
        modifier = modifier,
        borderColor = EmeraldPrimary.copy(alpha = 0.55f),
        glowAccent = EmeraldPrimary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Hub,
                contentDescription = null,
                tint = EmeraldGlow,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "TECHNOLOGY PARTNER",
                style = MaterialTheme.typography.labelMedium,
                color = GoldBright
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = siteSettings.eaConnectHeading,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = R.drawable.img_ea_mobile_connect),
            contentDescription = "EA Mobile Connect futuristic trading connectivity",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = siteSettings.eaConnectDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = ObsidianBlack.copy(alpha = 0.7f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Powering both PRAISE SCALPER PRO V6+ ENGINE and GILBERT SCALPER PRO V6+ ENGINE.",
                style = MaterialTheme.typography.labelMedium,
                color = EmeraldGlow,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun RecommendedBrokerSectionCard(
    siteSettings: SiteSettings,
    onVisitBroker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassFintechCard(
        modifier = modifier,
        borderColor = GoldAccent.copy(alpha = 0.6f),
        glowAccent = GoldAccent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalance,
                contentDescription = null,
                tint = GoldBright,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "OUR RECOMMENDED BROKER",
                style = MaterialTheme.typography.labelMedium,
                color = GoldBright
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = siteSettings.recommendedBrokerHeading,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = siteSettings.recommendedBrokerDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = ObsidianBlack,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "OFFICIAL BROKER PARTNER LINK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = siteSettings.recommendedBrokerUrl,
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldGlow
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = { onVisitBroker(siteSettings.recommendedBrokerUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("visit_recommended_broker_button"),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent,
                contentColor = ObsidianBlack
            )
        ) {
            Text(
                text = "VISIT RECOMMENDED BROKER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AboutPraiseTradersSectionCard(
    siteSettings: SiteSettings,
    modifier: Modifier = Modifier
) {
    GlassFintechCard(
        modifier = modifier,
        borderColor = GlassBorder,
        glowAccent = EmeraldPrimary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = null,
                tint = EmeraldGlow,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "ABOUT PRAISETRADERS",
                style = MaterialTheme.typography.labelMedium,
                color = GoldBright
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = siteSettings.aboutHeading,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = siteSettings.aboutDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}

@Composable
fun ContactDesksSectionCard(
    bots: List<TradingBot>,
    onContactBotWhatsApp: (TradingBot) -> Unit,
    modifier: Modifier = Modifier,
    extraContent: @Composable () -> Unit = {}
) {
    GlassFintechCard(
        modifier = modifier,
        borderColor = EmeraldPrimary.copy(alpha = 0.5f),
        glowAccent = EmeraldPrimary
    ) {
        SectionHeader(
            eyebrow = "DIRECT WHATSAPP DESKS",
            title = "CONTACT US",
            subtitle = "Connect directly with the dedicated WhatsApp desk for each trading bot."
        )
        Spacer(modifier = Modifier.height(16.dp))

        bots.forEachIndexed { idx, bot ->
            if (idx > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = GlassBorder)
                Spacer(modifier = Modifier.height(12.dp))
            }
            val isSapphire = bot.accentTheme == BotAccentTheme.SAPPHIRE.key
            val accent = if (isSapphire) SapphireBlue else EmeraldPrimary
            val glow = if (isSapphire) SapphireGlow else EmeraldGlow

            Text(
                text = bot.name,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "WhatsApp: ${bot.whatsappDisplay}",
                style = MaterialTheme.typography.labelMedium,
                color = glow
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { onContactBotWhatsApp(bot) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("contact_whatsapp_${bot.id}"),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = ObsidianBlack
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WHATSAPP ${bot.whatsappDisplay}",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        extraContent()
    }
}
