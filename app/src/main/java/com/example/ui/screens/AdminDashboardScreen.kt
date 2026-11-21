package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Announcement
import com.example.data.model.PromoOffer
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.UiState
import com.example.data.model.formatZar
import com.example.ui.components.GlassFintechCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminDashboardScreen(
    isAdmin: Boolean,
    currentUserEmail: String?,
    isBusy: Boolean,
    botsState: UiState<List<TradingBot>>,
    announcementsState: UiState<List<Announcement>>,
    offersState: UiState<List<PromoOffer>>,
    siteSettings: SiteSettings,
    onSignOut: () -> Unit,
    onSeedDefaults: () -> Unit,
    onPickImage: suspend (Uri) -> Result<String>,
    onSaveBot: (TradingBot, Boolean, () -> Unit) -> Unit,
    onToggleBotActive: (String, Boolean) -> Unit,
    onDeleteBot: (String) -> Unit,
    onSaveAnnouncement: (Announcement, Boolean, () -> Unit) -> Unit,
    onToggleAnnouncementActive: (String, Boolean) -> Unit,
    onDeleteAnnouncement: (String) -> Unit,
    onSaveOffer: (PromoOffer, Boolean, () -> Unit) -> Unit,
    onToggleOfferActive: (String, Boolean) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onSaveSettings: (SiteSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isAdmin) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ObsidianBlack)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassFintechCard(
                modifier = Modifier.widthIn(max = 520.dp),
                borderColor = GoldAccent.copy(alpha = 0.55f),
                glowAccent = GoldAccent
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "ADMINISTRATOR ACCESS REQUIRED",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldBright
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Signed in as: ${currentUserEmail ?: "Unknown account"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldGlow
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Only verified PraiseTraders administrators can modify trading bots, pricing, images, announcements, or broker settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                ) {
                    Text("SIGN OUT / SWITCH ACCOUNT", color = GoldBright)
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var editingBot by remember { mutableStateOf<Pair<TradingBot?, Boolean>?>(null) }
    var editingAnnouncement by remember { mutableStateOf<Pair<Announcement?, Boolean>?>(null) }
    var editingOffer by remember { mutableStateOf<Pair<PromoOffer?, Boolean>?>(null) }

    val bots = (botsState as? UiState.Success)?.data.orEmpty()
    val announcements = (announcementsState as? UiState.Success)?.data.orEmpty()
    val offers = (offersState as? UiState.Success)?.data.orEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "ADMIN DASHBOARD",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldBright
                        )
                    }
                    OutlinedButton(
                        onClick = onSeedDefaults,
                        enabled = !isBusy,
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGlow)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SYNC DEFAULTS", style = MaterialTheme.typography.labelSmall, color = EmeraldGlow)
                    }
                }
                Text(
                    text = "Verified Admin: ${currentUserEmail ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ObsidianSurface,
                contentColor = EmeraldGlow
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("BOTS (${bots.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("OFFERS & NEWS") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("SITE & BROKER") })
            }

            if (isBusy) {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = EmeraldPrimary)
                }
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Button(
                                onClick = { editingBot = null to true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_add_bot_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = ObsidianBlack
                                )
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ADD NEW TRADING BOT", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        items(bots, key = { it.id }) { bot ->
                            GlassFintechCard(
                                borderColor = if (bot.active) EmeraldPrimary.copy(alpha = 0.5f) else GlassBorder
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(bot.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                        Text(
                                            text = "${formatZar(bot.originalPriceZar)} → ${formatZar(bot.promoPriceZar)} | WA: ${bot.whatsappDisplay}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldGlow
                                        )
                                    }
                                    Switch(
                                        checked = bot.active,
                                        onCheckedChange = { checked -> onToggleBotActive(bot.id, checked) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(bot.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { editingBot = bot to false },
                                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldBright)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("EDIT / IMAGE", color = GoldBright, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { onDeleteBot(bot.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Bot", tint = DangerCrimson)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            SectionHeader(
                                eyebrow = "LIVE NOTICES",
                                title = "ANNOUNCEMENTS"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { editingAnnouncement = null to true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = ObsidianBlack)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ADD ANNOUNCEMENT", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        items(announcements, key = { it.id }) { ann ->
                            GlassFintechCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("[${ann.badgeLabel}] ${ann.title}", style = MaterialTheme.typography.titleMedium, color = GoldBright)
                                        Text(ann.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = ann.active,
                                        onCheckedChange = { onToggleAnnouncementActive(ann.id, it) }
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = { editingAnnouncement = ann to false }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Announcement", tint = GoldBright)
                                    }
                                    IconButton(onClick = { onDeleteAnnouncement(ann.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Announcement", tint = DangerCrimson)
                                    }
                                }
                            }
                        }

                        item {
                            HorizontalDivider(color = GlassBorder)
                            Spacer(modifier = Modifier.height(10.dp))
                            SectionHeader(
                                eyebrow = "BLACK FRIDAY & DEALS",
                                title = "PROMOTIONAL OFFERS",
                                accentColor = GoldAccent
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { editingOffer = null to true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = ObsidianBlack)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ADD PROMOTIONAL OFFER", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        items(offers, key = { it.id }) { offer ->
                            GlassFintechCard(borderColor = GoldAccent.copy(alpha = 0.45f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${offer.title} — ${offer.botName}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                        Text(
                                            text = "${formatZar(offer.originalPriceZar)} → ${formatZar(offer.promoPriceZar)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = EmeraldGlow
                                        )
                                    }
                                    Switch(
                                        checked = offer.active,
                                        onCheckedChange = { onToggleOfferActive(offer.id, it) }
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = { editingOffer = offer to false }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Offer", tint = GoldBright)
                                    }
                                    IconButton(onClick = { onDeleteOffer(offer.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Offer", tint = DangerCrimson)
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    SiteSettingsEditorTab(
                        siteSettings = siteSettings,
                        onSaveSettings = onSaveSettings
                    )
                }
            }
        }
    }

    editingBot?.let { (bot, isNew) ->
        BotEditorDialog(
            initialBot = bot,
            onPickImage = onPickImage,
            onDismiss = { editingBot = null },
            onSave = { updated, createNew ->
                onSaveBot(updated, createNew) { editingBot = null }
            }
        )
    }

    editingAnnouncement?.let { (ann, isNew) ->
        AnnouncementEditorDialog(
            initial = ann,
            onDismiss = { editingAnnouncement = null },
            onSave = { updated, createNew ->
                onSaveAnnouncement(updated, createNew) { editingAnnouncement = null }
            }
        )
    }

    editingOffer?.let { (offer, isNew) ->
        PromoOfferEditorDialog(
            initial = offer,
            onDismiss = { editingOffer = null },
            onSave = { updated, createNew ->
                onSaveOffer(updated, createNew) { editingOffer = null }
            }
        )
    }
}

@Composable
private fun SiteSettingsEditorTab(
    siteSettings: SiteSettings,
    onSaveSettings: (SiteSettings) -> Unit
) {
    var brokerUrl by remember(siteSettings) { mutableStateOf(siteSettings.recommendedBrokerUrl) }
    var brokerHeading by remember(siteSettings) { mutableStateOf(siteSettings.recommendedBrokerHeading) }
    var brokerDesc by remember(siteSettings) { mutableStateOf(siteSettings.recommendedBrokerDescription) }
    var bfHeading by remember(siteSettings) { mutableStateOf(siteSettings.blackFridayHeading) }
    var bfSlogan by remember(siteSettings) { mutableStateOf(siteSettings.blackFridaySlogan) }
    var bfNotice by remember(siteSettings) { mutableStateOf(siteSettings.blackFridayNotice) }
    var eaHeading by remember(siteSettings) { mutableStateOf(siteSettings.eaConnectHeading) }
    var eaDesc by remember(siteSettings) { mutableStateOf(siteSettings.eaConnectDescription) }
    var aboutHeading by remember(siteSettings) { mutableStateOf(siteSettings.aboutHeading) }
    var aboutDesc by remember(siteSettings) { mutableStateOf(siteSettings.aboutDescription) }
    var disclaimer by remember(siteSettings) { mutableStateOf(siteSettings.riskDisclaimer) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            GlassFintechCard(borderColor = GoldAccent.copy(alpha = 0.5f)) {
                Text("RECOMMENDED BROKER SETTINGS", style = MaterialTheme.typography.titleMedium, color = GoldBright)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = brokerUrl,
                    onValueChange = { brokerUrl = it },
                    label = { Text("Recommended Broker URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = brokerHeading,
                    onValueChange = { brokerHeading = it },
                    label = { Text("Broker Heading") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = brokerDesc,
                    onValueChange = { brokerDesc = it },
                    label = { Text("Broker Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            GlassFintechCard {
                Text("BLACK FRIDAY & BRAND COPY", style = MaterialTheme.typography.titleMedium, color = EmeraldGlow)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = bfHeading, onValueChange = { bfHeading = it }, label = { Text("Black Friday Heading") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = bfSlogan, onValueChange = { bfSlogan = it }, label = { Text("Black Friday Slogan") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = bfNotice, onValueChange = { bfNotice = it }, label = { Text("Black Friday Notice") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = eaHeading, onValueChange = { eaHeading = it }, label = { Text("EA Mobile Connect Heading") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = eaDesc, onValueChange = { eaDesc = it }, label = { Text("EA Mobile Connect Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = aboutHeading, onValueChange = { aboutHeading = it }, label = { Text("About Heading") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = aboutDesc, onValueChange = { aboutDesc = it }, label = { Text("About Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = disclaimer, onValueChange = { disclaimer = it }, label = { Text("Trading Risk Disclaimer") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        }

        if (errorMsg != null) {
            item {
                Text(errorMsg ?: "", color = DangerCrimson, style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Button(
                onClick = {
                    val trimmedUrl = brokerUrl.trim()
                    if (!trimmedUrl.startsWith("https://") && !trimmedUrl.startsWith("http://")) {
                        errorMsg = "Broker URL must start with https:// or http://"
                    } else {
                        errorMsg = null
                        onSaveSettings(
                            siteSettings.copy(
                                recommendedBrokerUrl = trimmedUrl,
                                recommendedBrokerHeading = brokerHeading.trim(),
                                recommendedBrokerDescription = brokerDesc.trim(),
                                blackFridayHeading = bfHeading.trim(),
                                blackFridaySlogan = bfSlogan.trim(),
                                blackFridayNotice = bfNotice.trim(),
                                eaConnectHeading = eaHeading.trim(),
                                eaConnectDescription = eaDesc.trim(),
                                aboutHeading = aboutHeading.trim(),
                                aboutDescription = aboutDesc.trim(),
                                riskDisclaimer = disclaimer.trim()
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("admin_save_settings_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = ObsidianBlack
                )
            ) {
                Text("SAVE SITE & BROKER SETTINGS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
