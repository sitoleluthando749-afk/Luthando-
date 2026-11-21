package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.UiState
import com.example.ui.components.GlassFintechCard
import com.example.ui.components.RiskDisclaimerAndBrandFooter
import com.example.ui.components.SectionHeader
import com.example.ui.components.TradingBotShowcaseCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BotsScreen(
    botsState: UiState<List<TradingBot>>,
    siteSettings: SiteSettings,
    onBuyBot: (TradingBot) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SectionHeader(
                    eyebrow = "AUTOMATED EXPERT ADVISORS",
                    title = "OUR PREMIUM TRADING BOTS",
                    subtitle = "Choose your preferred algorithmic engine below. Every purchase button routes directly to its dedicated official WhatsApp desk."
                )
            }

            item {
                Surface(
                    color = EmeraldDeep.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = null,
                            tint = EmeraldGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "POWERED BY EA MOBILE CONNECT — Automated connectivity & modern EA technology.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }

            when (botsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = EmeraldPrimary)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        GlassFintechCard(borderColor = MaterialTheme.colorScheme.error) {
                            Text(
                                text = botsState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    val activeBots = botsState.data.filter { it.active }
                    if (activeBots.isEmpty()) {
                        item {
                            GlassFintechCard {
                                Text(
                                    text = "No active trading bots are currently published.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Check back soon or activate products from the Admin Dashboard.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        item {
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val isWide = maxWidth >= 680.dp
                                if (isWide) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        activeBots.chunked(2).forEach { pair ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                pair.forEach { bot ->
                                                    TradingBotShowcaseCard(
                                                        bot = bot,
                                                        onBuyClicked = onBuyBot,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        activeBots.forEach { bot ->
                                            TradingBotShowcaseCard(
                                                bot = bot,
                                                onBuyClicked = onBuyBot
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                RiskDisclaimerAndBrandFooter(
                    riskDisclaimer = siteSettings.riskDisclaimer
                )
            }
        }
    }
}
