package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Announcement
import com.example.data.model.DefaultCatalog
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.UiState
import com.example.data.model.formatZar
import com.example.ui.components.AboutPraiseTradersSectionCard
import com.example.ui.components.BlackFridayTopPromoBanner
import com.example.ui.components.ContactDesksSectionCard
import com.example.ui.components.EaMobileConnectSectionCard
import com.example.ui.components.GlassFintechCard
import com.example.ui.components.PraiseTradersHeaderBanner
import com.example.ui.components.RecommendedBrokerSectionCard
import com.example.ui.components.RiskDisclaimerAndBrandFooter
import com.example.ui.components.SectionHeader
import com.example.ui.components.TradingBotShowcaseCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    botsState: UiState<List<TradingBot>>,
    announcementsState: UiState<List<Announcement>>,
    siteSettings: SiteSettings,
    onExploreBots: () -> Unit,
    onViewBlackFriday: () -> Unit,
    onContactUs: () -> Unit,
    onVisitBroker: (String) -> Unit,
    onBuyBot: (TradingBot) -> Unit,
    onContactBotWhatsApp: (TradingBot) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var activeSectionIndex by remember { mutableIntStateOf(0) }

    val activeAnnouncements = (announcementsState as? UiState.Success)?.data?.filter { it.active }.orEmpty()
    val hasAnnouncements = activeAnnouncements.isNotEmpty()
    val botsList = (botsState as? UiState.Success)?.data?.filter { it.active }
        ?.takeIf { it.isNotEmpty() } ?: DefaultCatalog.defaultBots

    val botsHeaderIdx = if (hasAnnouncements) 5 else 4
    val saleIdx = botsHeaderIdx + 2
    val eaConnectIdx = saleIdx + 1
    val brokerIdx = eaConnectIdx + 1
    val aboutIdx = brokerIdx + 1
    val contactIdx = aboutIdx + 1

    val sectionQuickLinks = listOf(
        "HOME" to 0,
        "OUR BOTS" to botsHeaderIdx,
        "BLACK FRIDAY" to saleIdx,
        "EA CONNECT" to eaConnectIdx,
        "BROKER" to brokerIdx,
        "ABOUT US" to aboutIdx,
        "CONTACT US" to contactIdx
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 0. Branded PraiseTraders Header
            item {
                PraiseTradersHeaderBanner(
                    modifier = Modifier.testTag("home_brand_header")
                )
            }

            // 1. High-Impact Black Friday Deals Promotional Banner with Countdown & Claim Offer
            item {
                BlackFridayTopPromoBanner(
                    siteSettings = siteSettings,
                    onClaimOffer = onViewBlackFriday
                )
            }

            // 2. Scrollable Quick-Jump Section Navigation Bar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_section_nav_bar")
                ) {
                    itemsIndexed(sectionQuickLinks) { index, (label, targetIdx) ->
                        FilterChip(
                            selected = activeSectionIndex == index,
                            onClick = {
                                activeSectionIndex = index
                                scope.launch { listState.animateScrollToItem(targetIdx) }
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ObsidianSurface,
                                labelColor = TextSecondary,
                                selectedContainerColor = EmeraldDeep,
                                selectedLabelColor = GoldBright
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = activeSectionIndex == index,
                                borderColor = EmeraldPrimary.copy(alpha = 0.35f),
                                selectedBorderColor = GoldAccent
                            )
                        )
                    }
                }
            }

            // 2. Luxury Dubai Skyscraper Hero Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(ObsidianBlack)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_dubai_skyline),
                                contentDescription = "PRAISETRADERS illuminated on Dubai skyscraper at night",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                ObsidianBlack.copy(alpha = 0.08f),
                                                ObsidianBlack.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            Surface(
                                color = EmeraldDeep.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, EmeraldPrimary),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = EmeraldGlow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "ALGORITHMIC EXPERT ADVISORS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldGlow
                                    )
                                }
                            }
                        }

                        GlassFintechCard(
                            borderColor = GoldAccent.copy(alpha = 0.55f),
                            glowAccent = EmeraldPrimary
                        ) {
                            Text(
                                text = stringResource(R.string.hero_welcome),
                                style = MaterialTheme.typography.displayMedium,
                                color = GoldBright
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.hero_subtitle),
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldGlow
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.hero_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = onExploreBots,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("hero_explore_bots_button"),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = ObsidianBlack
                                )
                            ) {
                                Icon(Icons.Filled.Memory, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.cta_explore_bots),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onViewBlackFriday,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("hero_black_friday_button"),
                                    shape = MaterialTheme.shapes.small,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldAccent,
                                        contentColor = ObsidianBlack
                                    )
                                ) {
                                    Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "BLACK FRIDAY DEALS",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = onContactUs,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("hero_contact_us_button"),
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGlow)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.cta_contact_us),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Live Announcements Banner (optional)
            if (hasAnnouncements) {
                item {
                    GlassFintechCard(
                        borderColor = GoldAccent.copy(alpha = 0.45f),
                        glowAccent = GoldAccent
                    ) {
                        activeAnnouncements.take(3).forEachIndexed { idx, item ->
                            if (idx > 0) Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = GoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Surface(
                                    color = GoldDeep,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = item.badgeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldBright,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 4. Featured Trading Bots Header
            item {
                SectionHeader(
                    eyebrow = "OUR PREMIUM TRADING BOTS",
                    title = "FLAGSHIP EXPERT ADVISORS",
                    subtitle = "Official automated trading engines powered by EA MOBILE CONNECT."
                )
            }

            // 5. Trading Bots Grid / List
            when (botsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
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

            // 6. Black Friday Mega Sale Spotlight
            item {
                GlassFintechCard(
                    borderColor = GoldAccent.copy(alpha = 0.65f),
                    glowAccent = GoldAccent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = siteSettings.blackFridayHeading,
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldBright
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = siteSettings.blackFridaySlogan,
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldGlow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = siteSettings.blackFridayNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = formatZar(1500L),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = TextMuted
                            )
                            Text(
                                text = "→ ${formatZar(200L)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = EmeraldGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = onViewBlackFriday,
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = ObsidianBlack
                            )
                        ) {
                            Text("VIEW SALE", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 7. EA Mobile Connect Section
            item {
                EaMobileConnectSectionCard(siteSettings = siteSettings)
            }

            // 8. Recommended Broker Section
            item {
                RecommendedBrokerSectionCard(
                    siteSettings = siteSettings,
                    onVisitBroker = onVisitBroker
                )
            }

            // 9. About PraiseTraders Section
            item {
                AboutPraiseTradersSectionCard(siteSettings = siteSettings)
            }

            // 10. Contact Us Section
            item {
                ContactDesksSectionCard(
                    bots = botsList,
                    onContactBotWhatsApp = onContactBotWhatsApp
                )
            }

            // 11. Trust & Transparency Disclaimer + Final Brand Message
            item {
                RiskDisclaimerAndBrandFooter(
                    riskDisclaimer = siteSettings.riskDisclaimer
                )
            }
        }
    }
}
