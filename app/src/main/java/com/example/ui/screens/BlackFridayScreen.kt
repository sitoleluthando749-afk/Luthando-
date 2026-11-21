package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.BotAccentTheme
import com.example.data.model.PromoOffer
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.UiState
import com.example.data.model.formatZar
import com.example.ui.components.GlassFintechCard
import com.example.ui.components.RiskDisclaimerAndBrandFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.SapphireGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BlackFridayScreen(
    botsState: UiState<List<TradingBot>>,
    offersState: UiState<List<PromoOffer>>,
    siteSettings: SiteSettings,
    onBuyBot: (TradingBot) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBots = (botsState as? UiState.Success)?.data?.filter { it.active }.orEmpty()
    val activeOffers = (offersState as? UiState.Success)?.data?.filter { it.active }.orEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Black Friday Header Card
            item {
                GlassFintechCard(
                    borderColor = GoldAccent.copy(alpha = 0.7f),
                    glowAccent = GoldAccent
                ) {
                    Surface(
                        color = GoldDeep,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, GoldAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "LIMITED PROMOTIONAL EVENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = siteSettings.blackFridayHeading,
                        style = MaterialTheme.typography.displayMedium,
                        color = GoldBright
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = siteSettings.blackFridaySlogan,
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldGlow
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = EmeraldDeep.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = EmeraldGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = siteSettings.blackFridayNotice,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    eyebrow = "OFFICIAL BLACK FRIDAY PRICING",
                    title = "FEATURED TRADING BOT DEALS",
                    subtitle = "Tap any bot below to proceed directly to its dedicated WhatsApp purchasing channel.",
                    accentColor = GoldAccent
                )
            }

            items(activeBots, key = { it.id }) { bot ->
                val isSapphire = bot.accentTheme == BotAccentTheme.SAPPHIRE.key
                val accent = if (isSapphire) SapphireBlue else EmeraldPrimary
                val glow = if (isSapphire) SapphireGlow else EmeraldGlow

                GlassFintechCard(
                    borderColor = accent.copy(alpha = 0.55f),
                    glowAccent = accent
                ) {
                    Text(
                        text = bot.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bot.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = glow
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GlassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STANDARD PRICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = formatZar(bot.originalPriceZar),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = TextMuted
                            )
                        }
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.headlineLarge,
                            color = GoldBright
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BLACK FRIDAY SPECIAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldBright
                            )
                            Text(
                                text = formatZar(bot.promoPriceZar),
                                style = MaterialTheme.typography.displayMedium,
                                color = glow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onBuyBot(bot) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = ObsidianBlack
                        )
                    ) {
                        Text(bot.buttonLabel, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (activeOffers.isNotEmpty()) {
                item {
                    SectionHeader(
                        eyebrow = "ADDITIONAL CAMPAIGNS",
                        title = "ACTIVE PROMOTIONAL OFFERS",
                        accentColor = EmeraldPrimary
                    )
                }

                items(activeOffers, key = { it.id }) { offer ->
                    GlassFintechCard(borderColor = GoldAccent.copy(alpha = 0.45f)) {
                        Text(
                            text = offer.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldBright
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = offer.botName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        if (offer.tagline.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = offer.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = formatZar(offer.originalPriceZar),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = TextMuted
                            )
                            Text(
                                text = "→ ${formatZar(offer.promoPriceZar)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = EmeraldGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                RiskDisclaimerAndBrandFooter(riskDisclaimer = siteSettings.riskDisclaimer)
            }
        }
    }
}
