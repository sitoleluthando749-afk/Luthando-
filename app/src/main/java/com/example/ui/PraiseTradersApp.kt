package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.DefaultCatalog
import com.example.data.model.InquiryChannel
import com.example.data.model.TradingBot
import com.example.data.model.UiState
import com.example.data.repository.PraiseTradersRepository
import com.example.data.repository.signOut
import com.example.ui.components.PraiseTradersBrandMark
import com.example.ui.components.openExternalUrl
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BlackFridayScreen
import com.example.ui.screens.BotsScreen
import com.example.ui.screens.EcosystemAnchor
import com.example.ui.screens.EcosystemScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PraiseTradersViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_BOTS = "bots"
const val ROUTE_BLACK_FRIDAY = "black_friday"
const val ROUTE_ECOSYSTEM = "ecosystem"
const val ROUTE_ADMIN = "admin"

private data class PrimaryNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val primaryNavItems = listOf(
    PrimaryNavItem(ROUTE_HOME, "HOME", Icons.Filled.Home, Icons.Outlined.Home),
    PrimaryNavItem(ROUTE_BOTS, "OUR BOTS", Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    PrimaryNavItem(ROUTE_BLACK_FRIDAY, "SALE", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer),
    PrimaryNavItem(ROUTE_ECOSYSTEM, "CONNECT", Icons.Filled.Hub, Icons.Outlined.Hub),
    PrimaryNavItem(ROUTE_ADMIN, "ADMIN", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
)

@Composable
fun PraiseTradersApp() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        key(user.uid) {
            AuthenticatedPraiseTradersShell(
                currentUserId = user.uid,
                currentUserEmail = user.email,
                isEmailVerified = user.isEmailVerified
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedPraiseTradersShell(
    currentUserId: String,
    currentUserEmail: String?,
    isEmailVerified: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: PraiseTradersViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val repo = PraiseTradersRepository(db)
                PraiseTradersViewModel(
                    repository = repo,
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail,
                    isEmailVerified = isEmailVerified
                )
            }
        }
    )

    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val botsState by viewModel.botsState.collectAsStateWithLifecycle()
    val announcementsState by viewModel.announcementsState.collectAsStateWithLifecycle()
    val offersState by viewModel.promoOffersState.collectAsStateWithLifecycle()
    val settingsState by viewModel.siteSettingsState.collectAsStateWithLifecycle()
    val inquiriesState by viewModel.inquiriesState.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val siteSettings = (settingsState as? UiState.Success)?.data ?: DefaultCatalog.defaultSiteSettings

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedEcosystemAnchor by remember { mutableStateOf<EcosystemAnchor?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    fun navigateToRoute(route: String, anchor: EcosystemAnchor? = null) {
        selectedEcosystemAnchor = anchor
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val handleBuyBot: (TradingBot) -> Unit = { bot ->
        viewModel.recordInquiry(
            botName = bot.name,
            whatsappNumber = bot.whatsappDisplay,
            channel = InquiryChannel.WHATSAPP_BUY,
            priceZar = bot.promoPriceZar
        )
        openExternalUrl(context, bot.buildWhatsAppPurchaseUrl())
    }

    val handleContactBot: (TradingBot) -> Unit = { bot ->
        viewModel.recordInquiry(
            botName = bot.name,
            whatsappNumber = bot.whatsappDisplay,
            channel = InquiryChannel.WHATSAPP_CONTACT,
            priceZar = bot.promoPriceZar
        )
        openExternalUrl(context, bot.buildWhatsAppContactUrl())
    }

    val handleVisitBroker: (String) -> Unit = { url ->
        viewModel.recordInquiry(
            botName = "Recommended Broker",
            whatsappNumber = url,
            channel = InquiryChannel.BROKER_VISIT,
            priceZar = 0L
        )
        openExternalUrl(context, url)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ObsidianSurface,
                drawerContentColor = TextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "PRAISETRADERS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldBright
                    )
                    Text(
                        text = "ALGORITHMIC TRADING SUITE",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGlow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentUserEmail ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                HorizontalDivider(color = GlassBorder)
                Spacer(modifier = Modifier.height(8.dp))

                val drawerItems = listOf(
                    Triple("HOME", Icons.Filled.Home) { navigateToRoute(ROUTE_HOME) },
                    Triple("ABOUT US", Icons.Filled.Business) { navigateToRoute(ROUTE_ECOSYSTEM, EcosystemAnchor.ABOUT) },
                    Triple("OUR BOTS", Icons.Filled.SmartToy) { navigateToRoute(ROUTE_BOTS) },
                    Triple("BLACK FRIDAY SALE", Icons.Filled.LocalOffer) { navigateToRoute(ROUTE_BLACK_FRIDAY) },
                    Triple("EA MOBILE CONNECT", Icons.Filled.Memory) { navigateToRoute(ROUTE_ECOSYSTEM, EcosystemAnchor.EA_CONNECT) },
                    Triple("RECOMMENDED BROKER", Icons.Filled.AccountBalance) { navigateToRoute(ROUTE_ECOSYSTEM, EcosystemAnchor.BROKER) },
                    Triple("CONTACT US", Icons.AutoMirrored.Filled.Chat) { navigateToRoute(ROUTE_ECOSYSTEM, EcosystemAnchor.CONTACT) },
                    Triple("ADMIN DASHBOARD", Icons.Filled.AdminPanelSettings) { navigateToRoute(ROUTE_ADMIN) }
                )

                drawerItems.forEach { (label, icon, action) ->
                    NavigationDrawerItem(
                        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            action()
                        },
                        icon = { Icon(icon, contentDescription = null, tint = EmeraldGlow) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = ObsidianSurface,
                            unselectedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = GlassBorder)
                NavigationDrawerItem(
                    label = { Text("SIGN OUT", style = MaterialTheme.typography.labelLarge) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        signOut(credentialManager, scope)
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = GoldBright) },
                    modifier = Modifier.padding(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = ObsidianSurface,
                        unselectedTextColor = GoldBright
                    )
                )
            }
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBlack)
        ) {
            val useRail = maxWidth >= 680.dp

            Scaffold(
                containerColor = ObsidianBlack,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            PraiseTradersBrandMark(subtitle = "EA MOBILE CONNECT")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_navigation_drawer")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Open Navigation Menu",
                                    tint = TextPrimary
                                )
                            }
                        },
                        actions = {
                            if (isAdmin) {
                                Surface(
                                    color = EmeraldDeep,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                                    onClick = { navigateToRoute(ROUTE_ADMIN) },
                                    modifier = Modifier.padding(end = 10.dp)
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldBright,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = ObsidianSurface
                        )
                    )
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = ObsidianSurface,
                            contentColor = TextPrimary,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            primaryNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = EmeraldGlow,
                                        selectedTextColor = GoldBright,
                                        indicatorColor = EmeraldDeep,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = ObsidianSurface,
                            contentColor = TextPrimary,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            primaryNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = EmeraldGlow,
                                        selectedTextColor = GoldBright,
                                        indicatorColor = EmeraldDeep,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    )
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                botsState = botsState,
                                announcementsState = announcementsState,
                                siteSettings = siteSettings,
                                onExploreBots = { navigateToRoute(ROUTE_BOTS) },
                                onViewBlackFriday = { navigateToRoute(ROUTE_BLACK_FRIDAY) },
                                onContactUs = { navigateToRoute(ROUTE_ECOSYSTEM, EcosystemAnchor.CONTACT) },
                                onVisitBroker = handleVisitBroker,
                                onBuyBot = handleBuyBot,
                                onContactBotWhatsApp = handleContactBot
                            )
                        }
                        composable(ROUTE_BOTS) {
                            BotsScreen(
                                botsState = botsState,
                                siteSettings = siteSettings,
                                onBuyBot = handleBuyBot
                            )
                        }
                        composable(ROUTE_BLACK_FRIDAY) {
                            BlackFridayScreen(
                                botsState = botsState,
                                offersState = offersState,
                                siteSettings = siteSettings,
                                onBuyBot = handleBuyBot
                            )
                        }
                        composable(ROUTE_ECOSYSTEM) {
                            EcosystemScreen(
                                initialAnchor = selectedEcosystemAnchor,
                                botsState = botsState,
                                siteSettings = siteSettings,
                                inquiriesState = inquiriesState,
                                onVisitBroker = handleVisitBroker,
                                onContactBotWhatsApp = handleContactBot
                            )
                        }
                        composable(ROUTE_ADMIN) {
                            AdminDashboardScreen(
                                isAdmin = isAdmin,
                                currentUserEmail = currentUserEmail,
                                isBusy = isBusy,
                                botsState = botsState,
                                announcementsState = announcementsState,
                                offersState = offersState,
                                siteSettings = siteSettings,
                                onSignOut = { signOut(credentialManager, scope) },
                                onSeedDefaults = { viewModel.seedOfficialDefaults() },
                                onPickImage = { uri -> viewModel.importPickedImage(context, uri) },
                                onSaveBot = { bot, isNew, onDone -> viewModel.saveBot(bot, isNew, onDone) },
                                onToggleBotActive = { id, active -> viewModel.toggleBotActive(id, active) },
                                onDeleteBot = { id -> viewModel.deleteBot(id) },
                                onSaveAnnouncement = { ann, isNew, onDone -> viewModel.saveAnnouncement(ann, isNew, onDone) },
                                onToggleAnnouncementActive = { id, active -> viewModel.toggleAnnouncementActive(id, active) },
                                onDeleteAnnouncement = { id -> viewModel.deleteAnnouncement(id) },
                                onSaveOffer = { offer, isNew, onDone -> viewModel.savePromoOffer(offer, isNew, onDone) },
                                onToggleOfferActive = { id, active -> viewModel.togglePromoOfferActive(id, active) },
                                onDeleteOffer = { id -> viewModel.deletePromoOffer(id) },
                                onSaveSettings = { settings -> viewModel.saveSiteSettings(settings) }
                            )
                        }
                    }
                }
            }
        }
    }
}
