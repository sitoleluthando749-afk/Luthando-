package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BotAccentTheme
import com.example.data.model.DefaultCatalog
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.UiState
import com.example.data.model.UserInquiry
import com.example.ui.components.GlassFintechCard
import com.example.ui.components.RiskDisclaimerAndBrandFooter
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.SapphireGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class EcosystemAnchor(val index: Int, val label: String) {
    EA_CONNECT(1, "EA MOBILE CONNECT"),
    BROKER(2, "RECOMMENDED BROKER"),
    ABOUT(3, "ABOUT US"),
    CONTACT(4, "CONTACT US")
}

@Composable
fun EcosystemScreen(
    initialAnchor: EcosystemAnchor?,
    botsState: UiState<List<TradingBot>>,
    siteSettings: SiteSettings,
    inquiriesState: UiState<List<UserInquiry>>,
    onVisitBroker: (String) -> Unit,
    onContactBotWhatsApp: (TradingBot) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialAnchor) {
        if (initialAnchor != null) {
            listState.animateScrollToItem(initialAnchor.index)
        }
    }

    val bots = (botsState as? UiState.Success)?.data?.filter { it.active }
        ?.takeIf { it.isNotEmpty() } ?: DefaultCatalog.defaultBots

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 0: Quick Jump Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EcosystemAnchor.entries.take(2).forEach { anchor ->
                        FilterChip(
                            selected = initialAnchor == anchor,
                            onClick = { scope.launch { listState.animateScrollToItem(anchor.index) } },
                            label = { Text(anchor.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ObsidianSurface,
                                labelColor = TextSecondary,
                                selectedContainerColor = EmeraldDeep,
                                selectedLabelColor = EmeraldGlow
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EcosystemAnchor.entries.drop(2).forEach { anchor ->
                        FilterChip(
                            selected = initialAnchor == anchor,
                            onClick = { scope.launch { listState.animateScrollToItem(anchor.index) } },
                            label = { Text(anchor.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ObsidianSurface,
                                labelColor = TextSecondary,
                                selectedContainerColor = EmeraldDeep,
                                selectedLabelColor = EmeraldGlow
                            )
                        )
                    }
                }
            }

            // 1: EA MOBILE CONNECT
            item {
                GlassFintechCard(
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

            // 2: OUR RECOMMENDED BROKER
            item {
                GlassFintechCard(
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

            // 3: ABOUT PRAISETRADERS (WHO WE ARE)
            item {
                GlassFintechCard(
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

            // 4: CONTACT US
            item {
                GlassFintechCard(
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

                    if (inquiriesState is UiState.Success && inquiriesState.data.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "YOUR RECENT ACTIVITY",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldBright
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        inquiriesState.data.take(5).forEach { inquiry ->
                            Text(
                                text = "• ${inquiry.botName} (${inquiry.whatsappNumber})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 5: Trust Disclaimer
            item {
                RiskDisclaimerAndBrandFooter(riskDisclaimer = siteSettings.riskDisclaimer)
            }
        }
    }
}
