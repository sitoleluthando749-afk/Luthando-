package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Announcement
import com.example.data.model.BotAccentTheme
import com.example.data.model.PresetImageKey
import com.example.data.model.PromoOffer
import com.example.data.model.TradingBot
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun BotEditorDialog(
    initialBot: TradingBot?,
    onPickImage: suspend (android.net.Uri) -> Result<String>,
    onDismiss: () -> Unit,
    onSave: (TradingBot, Boolean) -> Unit
) {
    val isNew = initialBot == null
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initialBot?.name ?: "") }
    var subtitle by remember { mutableStateOf(initialBot?.subtitle ?: "POWERED BY EA MOBILE CONNECT.") }
    var description by remember { mutableStateOf(initialBot?.description ?: "") }
    var originalPriceText by remember { mutableStateOf((initialBot?.originalPriceZar ?: 1500L).toString()) }
    var promoPriceText by remember { mutableStateOf((initialBot?.promoPriceZar ?: 200L).toString()) }
    var whatsappText by remember { mutableStateOf(initialBot?.whatsappDisplay ?: "") }
    var buttonLabel by remember { mutableStateOf(initialBot?.buttonLabel ?: "") }
    var accentTheme by remember { mutableStateOf(initialBot?.accentTheme ?: BotAccentTheme.EMERALD.key) }
    var presetImageKey by remember { mutableStateOf(initialBot?.presetImageKey ?: PresetImageKey.PRAISE_SCALPER.key) }
    var imageUrl by remember { mutableStateOf(initialBot?.imageUrl ?: "") }
    var localImageName by remember { mutableStateOf(initialBot?.localImageName ?: "") }
    var active by remember { mutableStateOf(initialBot?.active ?: true) }
    var sortOrderText by remember { mutableStateOf((initialBot?.sortOrder ?: 0L).toString()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = onPickImage(uri)
                if (res.isSuccess) {
                    localImageName = res.getOrDefault("")
                    presetImageKey = PresetImageKey.CUSTOM.key
                    validationError = null
                } else {
                    validationError = res.exceptionOrNull()?.localizedMessage ?: "Image import failed"
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Text(
                text = if (isNew) "ADD NEW TRADING BOT" else "EDIT TRADING BOT",
                style = MaterialTheme.typography.titleLarge,
                color = GoldBright
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bot Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Technology Subtitle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = originalPriceText,
                        onValueChange = { originalPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Original (R)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = promoPriceText,
                        onValueChange = { promoPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Promo (R)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = whatsappText,
                    onValueChange = { whatsappText = it },
                    label = { Text("WhatsApp Number (e.g. 069 224 7990)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = buttonLabel,
                    onValueChange = { buttonLabel = it },
                    label = { Text("Purchase Button Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Accent Theme", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BotAccentTheme.entries.forEach { theme ->
                        FilterChip(
                            selected = accentTheme == theme.key,
                            onClick = { accentTheme = theme.key },
                            label = { Text(theme.key, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Product Image", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("UPLOAD / REPLACE FROM GALLERY")
                }

                if (localImageName.isNotBlank()) {
                    Text(
                        text = "Selected device image: $localImageName",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldPrimary
                    )
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = {
                        imageUrl = it
                        if (it.isNotBlank()) presetImageKey = PresetImageKey.CUSTOM.key
                    },
                    label = { Text("Or HTTPS Image URL (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Official Artwork Preset", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PresetImageKey.PRAISE_SCALPER, PresetImageKey.GILBERT_SCALPER).forEach { preset ->
                        FilterChip(
                            selected = presetImageKey == preset.key,
                            onClick = {
                                presetImageKey = preset.key
                                localImageName = ""
                                imageUrl = ""
                            },
                            label = {
                                Text(
                                    if (preset == PresetImageKey.PRAISE_SCALPER) "Green Robot" else "Blue Robot",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active / Published", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                OutlinedTextField(
                    value = sortOrderText,
                    onValueChange = { sortOrderText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Display Order (0, 1, 2...)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val orig = originalPriceText.toLongOrNull()
                    val promo = promoPriceText.toLongOrNull()
                    val order = sortOrderText.toLongOrNull() ?: 0L
                    when {
                        name.isBlank() -> validationError = "Bot name is required."
                        description.isBlank() -> validationError = "Description is required."
                        orig == null || promo == null -> validationError = "Enter valid numeric prices."
                        whatsappText.filter { it.isDigit() }.length < 8 ->
                            validationError = "Enter a valid WhatsApp number."
                        else -> {
                            val updated = (initialBot ?: TradingBot()).copy(
                                name = name.trim(),
                                subtitle = subtitle.trim().ifBlank { "POWERED BY EA MOBILE CONNECT." },
                                description = description.trim(),
                                originalPriceZar = orig,
                                promoPriceZar = promo,
                                whatsappDisplay = whatsappText.trim(),
                                whatsappE164 = whatsappText.filter { it.isDigit() },
                                buttonLabel = buttonLabel.trim().ifBlank { "BUY ${name.trim().uppercase()}" },
                                accentTheme = accentTheme,
                                presetImageKey = presetImageKey,
                                imageUrl = imageUrl.trim(),
                                localImageName = localImageName,
                                active = active,
                                sortOrder = order
                            )
                            onSave(updated, isNew)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = ObsidianBlack
                )
            ) {
                Text(if (isNew) "CREATE BOT" else "SAVE CHANGES")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AnnouncementEditorDialog(
    initial: Announcement?,
    onDismiss: () -> Unit,
    onSave: (Announcement, Boolean) -> Unit
) {
    val isNew = initial == null
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var message by remember { mutableStateOf(initial?.message ?: "") }
    var badgeLabel by remember { mutableStateOf(initial?.badgeLabel ?: "UPDATE") }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Text(
                text = if (isNew) "NEW ANNOUNCEMENT" else "EDIT ANNOUNCEMENT",
                style = MaterialTheme.typography.titleLarge,
                color = GoldBright
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Headline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = badgeLabel,
                    onValueChange = { badgeLabel = it },
                    label = { Text("Badge Label (e.g. BLACK FRIDAY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Published", color = TextPrimary)
                    Switch(checked = active, onCheckedChange = { active = it })
                }
                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        error = "Headline and message are required."
                    } else {
                        onSave(
                            (initial ?: Announcement()).copy(
                                title = title.trim(),
                                message = message.trim(),
                                badgeLabel = badgeLabel.trim().ifBlank { "UPDATE" },
                                active = active
                            ),
                            isNew
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = ObsidianBlack)
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextSecondary) }
        }
    )
}

@Composable
fun PromoOfferEditorDialog(
    initial: PromoOffer?,
    onDismiss: () -> Unit,
    onSave: (PromoOffer, Boolean) -> Unit
) {
    val isNew = initial == null
    var title by remember { mutableStateOf(initial?.title ?: "BLACK FRIDAY SPECIAL") }
    var botName by remember { mutableStateOf(initial?.botName ?: "") }
    var origText by remember { mutableStateOf((initial?.originalPriceZar ?: 1500L).toString()) }
    var promoText by remember { mutableStateOf((initial?.promoPriceZar ?: 200L).toString()) }
    var tagline by remember { mutableStateOf(initial?.tagline ?: "PREMIUM TRADING TECHNOLOGY. UNBELIEVABLE VALUE.") }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Text(
                text = if (isNew) "NEW PROMO OFFER" else "EDIT PROMO OFFER",
                style = MaterialTheme.typography.titleLarge,
                color = GoldBright
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Offer Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = botName,
                    onValueChange = { botName = it },
                    label = { Text("Product / Bot Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = origText,
                        onValueChange = { origText = it.filter { c -> c.isDigit() } },
                        label = { Text("Original (R)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = promoText,
                        onValueChange = { promoText = it.filter { c -> c.isDigit() } },
                        label = { Text("Promo (R)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active Offer", color = TextPrimary)
                    Switch(checked = active, onCheckedChange = { active = it })
                }
                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val o = origText.toLongOrNull()
                    val p = promoText.toLongOrNull()
                    if (title.isBlank() || botName.isBlank() || o == null || p == null) {
                        error = "All fields and numeric prices are required."
                    } else {
                        onSave(
                            (initial ?: PromoOffer()).copy(
                                title = title.trim(),
                                botName = botName.trim(),
                                originalPriceZar = o,
                                promoPriceZar = p,
                                tagline = tagline.trim(),
                                active = active
                            ),
                            isNew
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = ObsidianBlack)
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextSecondary) }
        }
    )
}
