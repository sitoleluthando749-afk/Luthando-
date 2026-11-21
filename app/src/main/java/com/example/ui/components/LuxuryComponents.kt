package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BotAccentTheme
import com.example.data.model.PresetImageKey
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
import com.example.ui.theme.SapphireDeep
import com.example.ui.theme.SapphireGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

fun openExternalUrl(context: Context, rawUrl: String) {
    val safeUrl = rawUrl.trim()
    if (safeUrl.isEmpty()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

@Composable
fun GlassFintechCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    glowAccent: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val brush = if (glowAccent != null) {
        Brush.verticalGradient(
            colors = listOf(
                glowAccent.copy(alpha = 0.14f),
                ObsidianCard.copy(alpha = 0.94f),
                ObsidianSurface.copy(alpha = 0.98f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                ObsidianCard.copy(alpha = 0.96f),
                ObsidianSurface.copy(alpha = 0.98f)
            )
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    accentColor: Color = EmeraldPrimary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = GoldBright
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun BotPosterImage(
    bot: TradingBot,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localFile = remember(bot.localImageName) {
        if (bot.localImageName.isNotBlank()) {
            File(File(context.filesDir, "bot_images"), bot.localImageName).takeIf { it.exists() }
        } else null
    }
    val fallbackDrawableRes = when (PresetImageKey.fromKey(bot.presetImageKey)) {
        PresetImageKey.PRAISE_SCALPER -> R.drawable.img_praise_scalper
        PresetImageKey.GILBERT_SCALPER -> R.drawable.img_gilbert_scalper
        PresetImageKey.DUBAI_SKYLINE -> R.drawable.img_dubai_skyline
        PresetImageKey.EA_MOBILE_CONNECT -> R.drawable.img_ea_mobile_connect
        PresetImageKey.CUSTOM -> if (bot.accentTheme == BotAccentTheme.SAPPHIRE.key) {
            R.drawable.img_gilbert_scalper
        } else {
            R.drawable.img_praise_scalper
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianBlack)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            localFile != null -> {
                AsyncImage(
                    model = localFile,
                    contentDescription = bot.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
            bot.imageUrl.isNotBlank() -> {
                AsyncImage(
                    model = bot.imageUrl,
                    contentDescription = bot.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = fallbackDrawableRes),
                    placeholder = painterResource(id = fallbackDrawableRes)
                )
            }
            else -> {
                Image(
                    painter = painterResource(id = fallbackDrawableRes),
                    contentDescription = bot.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun TradingBotShowcaseCard(
    bot: TradingBot,
    onBuyClicked: (TradingBot) -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(bot.id) { visible = true }

    val theme = BotAccentTheme.fromKey(bot.accentTheme)
    val accent = when (theme) {
        BotAccentTheme.EMERALD -> EmeraldPrimary
        BotAccentTheme.SAPPHIRE -> SapphireBlue
        BotAccentTheme.GOLD -> GoldAccent
    }
    val glow = when (theme) {
        BotAccentTheme.EMERALD -> EmeraldGlow
        BotAccentTheme.SAPPHIRE -> SapphireGlow
        BotAccentTheme.GOLD -> GoldBright
    }
    val badgeBg = when (theme) {
        BotAccentTheme.EMERALD -> EmeraldDeep
        BotAccentTheme.SAPPHIRE -> SapphireDeep
        BotAccentTheme.GOLD -> GoldDeep
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 })
    ) {
        GlassFintechCard(
            modifier = modifier.testTag("bot_card_${bot.id}"),
            borderColor = accent.copy(alpha = 0.5f),
            glowAccent = accent
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = glow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "BLACK FRIDAY DEAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = glow
                        )
                    }
                }
                Surface(
                    color = GoldDeep.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "EA MOBILE CONNECT",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldBright,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BotPosterImage(bot = bot)

            Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = bot.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GlassBorder)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "ORIGINAL PRICE",
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
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BLACK FRIDAY PRICE",
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "WhatsApp Number",
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "WhatsApp: ${bot.whatsappDisplay}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onBuyClicked(bot) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("buy_bot_${bot.id}"),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = ObsidianBlack
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = bot.buttonLabel,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun RiskDisclaimerAndBrandFooter(
    riskDisclaimer: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassFintechCard(
            borderColor = GoldAccent.copy(alpha = 0.4f),
            glowAccent = GoldAccent
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = "Trading Risk Disclaimer",
                    tint = GoldBright,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "TRUST & TRADING RISK DISCLAIMER",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldBright
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = riskDisclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ObsidianSurface,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.brand_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldBright
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "POWERING SMARTER TRADING THROUGH INNOVATION. PRECISION. TECHNOLOGY. INTELLIGENCE.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
