package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.Announcement
import com.example.data.model.DefaultCatalog
import com.example.data.model.InquiryChannel
import com.example.data.model.PromoOffer
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.UserInquiry
import com.example.data.model.normalizeSouthAfricanWhatsApp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class PraiseTradersRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "sitoleluthando749@gmail.com"
        private const val COLLECTION_BOTS = "trading_bots"
        private const val COLLECTION_ANNOUNCEMENTS = "announcements"
        private const val COLLECTION_OFFERS = "promo_offers"
        private const val COLLECTION_SETTINGS = "site_settings"
        private const val DOC_GLOBAL_SETTINGS = "global"
        private const val COLLECTION_ADMINS = "admins"
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_INQUIRIES = "inquiries"
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeIsAdmin(uid: String, email: String?, isEmailVerified: Boolean): Flow<Boolean> = flow {
        val bootstrapped = email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) && isEmailVerified
        if (bootstrapped) {
            emit(true)
            return@flow
        }
        val path = "$COLLECTION_ADMINS/$uid"
        emitAll(
            db.collection(COLLECTION_ADMINS).document(uid)
                .snapshots()
                .map { snapshot -> snapshot.exists() }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    emit(false)
                }
        )
    }

    fun observeBots(includeInactive: Boolean): Flow<List<TradingBot>> = flow {
        val uid = requireUserId()
        val query = if (includeInactive) {
            db.collection(COLLECTION_BOTS)
        } else {
            db.collection(COLLECTION_BOTS).whereEqualTo("active", true)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TradingBot::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.sortOrder }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, COLLECTION_BOTS)
                    }
                    throw error
                }
        )
    }

    fun observeAnnouncements(includeInactive: Boolean): Flow<List<Announcement>> = flow {
        val uid = requireUserId()
        val query = if (includeInactive) {
            db.collection(COLLECTION_ANNOUNCEMENTS)
        } else {
            db.collection(COLLECTION_ANNOUNCEMENTS).whereEqualTo("active", true)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Announcement::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, COLLECTION_ANNOUNCEMENTS)
                    }
                    throw error
                }
        )
    }

    fun observePromoOffers(includeInactive: Boolean): Flow<List<PromoOffer>> = flow {
        val uid = requireUserId()
        val query = if (includeInactive) {
            db.collection(COLLECTION_OFFERS)
        } else {
            db.collection(COLLECTION_OFFERS).whereEqualTo("active", true)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PromoOffer::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, COLLECTION_OFFERS)
                    }
                    throw error
                }
        )
    }

    fun observeSiteSettings(): Flow<Pair<SiteSettings, Boolean>> = flow {
        val uid = requireUserId()
        val path = "$COLLECTION_SETTINGS/$DOC_GLOBAL_SETTINGS"
        emitAll(
            db.collection(COLLECTION_SETTINGS).document(DOC_GLOBAL_SETTINGS)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        val parsed = snapshot.toObject(
                            SiteSettings::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ) ?: DefaultCatalog.defaultSiteSettings
                        parsed to true
                    } else {
                        DefaultCatalog.defaultSiteSettings to false
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeUserInquiries(userId: String): Flow<List<UserInquiry>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for inquiries." }
        val path = "$COLLECTION_USERS/$userId/$SUBCOLLECTION_INQUIRIES"
        emitAll(
            db.collection(COLLECTION_USERS).document(userId)
                .collection(SUBCOLLECTION_INQUIRIES)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        UserInquiry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun seedOfficialDefaultsIfNeeded(): Result<Unit> {
        return try {
            val uid = requireUserId()
            val settingsRef = db.collection(COLLECTION_SETTINGS).document(DOC_GLOBAL_SETTINGS)
            val settingsSnap = settingsRef.get().await()
            if (!settingsSnap.exists()) {
                val s = DefaultCatalog.defaultSiteSettings
                settingsRef.set(
                    mapOf(
                        "recommendedBrokerUrl" to s.recommendedBrokerUrl,
                        "recommendedBrokerHeading" to s.recommendedBrokerHeading,
                        "recommendedBrokerDescription" to s.recommendedBrokerDescription,
                        "blackFridayHeading" to s.blackFridayHeading,
                        "blackFridaySlogan" to s.blackFridaySlogan,
                        "blackFridayNotice" to s.blackFridayNotice,
                        "eaConnectHeading" to s.eaConnectHeading,
                        "eaConnectDescription" to s.eaConnectDescription,
                        "aboutHeading" to s.aboutHeading,
                        "aboutDescription" to s.aboutDescription,
                        "riskDisclaimer" to s.riskDisclaimer,
                        "updatedBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val botsSnap = db.collection(COLLECTION_BOTS).get().await()
            if (botsSnap.isEmpty) {
                DefaultCatalog.defaultBots.forEach { bot ->
                    db.collection(COLLECTION_BOTS).document(bot.id).set(
                        mapOf(
                            "name" to bot.name,
                            "subtitle" to bot.subtitle,
                            "description" to bot.description,
                            "originalPriceZar" to bot.originalPriceZar,
                            "promoPriceZar" to bot.promoPriceZar,
                            "whatsappDisplay" to bot.whatsappDisplay,
                            "whatsappE164" to bot.whatsappE164,
                            "buttonLabel" to bot.buttonLabel,
                            "accentTheme" to bot.accentTheme,
                            "presetImageKey" to bot.presetImageKey,
                            "imageUrl" to bot.imageUrl,
                            "localImageName" to bot.localImageName,
                            "active" to bot.active,
                            "sortOrder" to bot.sortOrder,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            val annSnap = db.collection(COLLECTION_ANNOUNCEMENTS).get().await()
            if (annSnap.isEmpty) {
                DefaultCatalog.defaultAnnouncements.forEach { ann ->
                    db.collection(COLLECTION_ANNOUNCEMENTS).document(ann.id).set(
                        mapOf(
                            "title" to ann.title,
                            "message" to ann.message,
                            "badgeLabel" to ann.badgeLabel,
                            "active" to ann.active,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            val offerSnap = db.collection(COLLECTION_OFFERS).get().await()
            if (offerSnap.isEmpty) {
                DefaultCatalog.defaultPromoOffers.forEach { offer ->
                    db.collection(COLLECTION_OFFERS).document(offer.id).set(
                        mapOf(
                            "title" to offer.title,
                            "botName" to offer.botName,
                            "originalPriceZar" to offer.originalPriceZar,
                            "promoPriceZar" to offer.promoPriceZar,
                            "tagline" to offer.tagline,
                            "active" to offer.active,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seedOfficialDefaults")
            Result.failure(e)
        }
    }

    suspend fun saveBot(bot: TradingBot, isNew: Boolean): Result<Unit> {
        val docId = if (isNew || bot.id.isBlank()) {
            "bot_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        } else {
            bot.id
        }
        val path = "$COLLECTION_BOTS/$docId"
        return try {
            requireUserId()
            val trimmedName = bot.name.trim().take(100)
            require(trimmedName.isNotEmpty()) { "Bot name is required." }
            val trimmedDesc = bot.description.trim().take(1000)
            require(trimmedDesc.isNotEmpty()) { "Bot description is required." }
            val (displayPhone, e164Phone) = normalizeSouthAfricanWhatsApp(bot.whatsappDisplay.ifBlank { bot.whatsappE164 })
            require(e164Phone.length in 8..20 && e164Phone.all { it.isDigit() }) {
                "Enter a valid WhatsApp phone number (e.g., 069 224 7990)."
            }
            val origPrice = bot.originalPriceZar.coerceIn(0L, 1_000_000L)
            val promoPrice = bot.promoPriceZar.coerceIn(0L, 1_000_000L)
            val buttonLabel = bot.buttonLabel.trim().ifBlank { "BUY $trimmedName" }.take(60)
            val subtitle = bot.subtitle.trim().ifBlank { "POWERED BY EA MOBILE CONNECT." }.take(120)
            val imageUrl = bot.imageUrl.trim().take(500)
            val localImageName = bot.localImageName.trim().take(200)

            val docRef = db.collection(COLLECTION_BOTS).document(docId)
            if (isNew || bot.id.isBlank()) {
                val payload = mapOf(
                    "name" to trimmedName,
                    "subtitle" to subtitle,
                    "description" to trimmedDesc,
                    "originalPriceZar" to origPrice,
                    "promoPriceZar" to promoPrice,
                    "whatsappDisplay" to displayPhone,
                    "whatsappE164" to e164Phone,
                    "buttonLabel" to buttonLabel,
                    "accentTheme" to bot.accentTheme,
                    "presetImageKey" to bot.presetImageKey,
                    "imageUrl" to imageUrl,
                    "localImageName" to localImageName,
                    "active" to bot.active,
                    "sortOrder" to bot.sortOrder.coerceIn(0L, 1000L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "name" to trimmedName,
                    "subtitle" to subtitle,
                    "description" to trimmedDesc,
                    "originalPriceZar" to origPrice,
                    "promoPriceZar" to promoPrice,
                    "whatsappDisplay" to displayPhone,
                    "whatsappE164" to e164Phone,
                    "buttonLabel" to buttonLabel,
                    "accentTheme" to bot.accentTheme,
                    "presetImageKey" to bot.presetImageKey,
                    "imageUrl" to imageUrl,
                    "localImageName" to localImageName,
                    "active" to bot.active,
                    "sortOrder" to bot.sortOrder.coerceIn(0L, 1000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleBotActive(botId: String, active: Boolean): Result<Unit> {
        val path = "$COLLECTION_BOTS/$botId"
        return try {
            requireUserId()
            require(botId.isNotBlank()) { "Bot ID cannot be blank." }
            db.collection(COLLECTION_BOTS).document(botId).update(
                mapOf(
                    "active" to active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteBot(botId: String): Result<Unit> {
        val path = "$COLLECTION_BOTS/$botId"
        return try {
            requireUserId()
            require(botId.isNotBlank()) { "Bot ID cannot be blank." }
            db.collection(COLLECTION_BOTS).document(botId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveAnnouncement(announcement: Announcement, isNew: Boolean): Result<Unit> {
        val docId = if (isNew || announcement.id.isBlank()) {
            "ann_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        } else {
            announcement.id
        }
        val path = "$COLLECTION_ANNOUNCEMENTS/$docId"
        return try {
            requireUserId()
            val title = announcement.title.trim().take(120)
            val message = announcement.message.trim().take(500)
            val badge = announcement.badgeLabel.trim().ifBlank { "UPDATE" }.take(40)
            require(title.isNotEmpty() && message.isNotEmpty()) { "Title and message are required." }

            val docRef = db.collection(COLLECTION_ANNOUNCEMENTS).document(docId)
            if (isNew || announcement.id.isBlank()) {
                docRef.set(
                    mapOf(
                        "title" to title,
                        "message" to message,
                        "badgeLabel" to badge,
                        "active" to announcement.active,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "title" to title,
                        "message" to message,
                        "badgeLabel" to badge,
                        "active" to announcement.active,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleAnnouncementActive(announcementId: String, active: Boolean): Result<Unit> {
        val path = "$COLLECTION_ANNOUNCEMENTS/$announcementId"
        return try {
            requireUserId()
            require(announcementId.isNotBlank())
            db.collection(COLLECTION_ANNOUNCEMENTS).document(announcementId).update(
                mapOf("active" to active, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(announcementId: String): Result<Unit> {
        val path = "$COLLECTION_ANNOUNCEMENTS/$announcementId"
        return try {
            requireUserId()
            require(announcementId.isNotBlank())
            db.collection(COLLECTION_ANNOUNCEMENTS).document(announcementId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun savePromoOffer(offer: PromoOffer, isNew: Boolean): Result<Unit> {
        val docId = if (isNew || offer.id.isBlank()) {
            "offer_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        } else {
            offer.id
        }
        val path = "$COLLECTION_OFFERS/$docId"
        return try {
            requireUserId()
            val title = offer.title.trim().take(120)
            val botName = offer.botName.trim().take(100)
            val tagline = offer.tagline.trim().take(200)
            require(title.isNotEmpty() && botName.isNotEmpty()) { "Offer title and bot name are required." }

            val docRef = db.collection(COLLECTION_OFFERS).document(docId)
            if (isNew || offer.id.isBlank()) {
                docRef.set(
                    mapOf(
                        "title" to title,
                        "botName" to botName,
                        "originalPriceZar" to offer.originalPriceZar.coerceIn(0L, 1_000_000L),
                        "promoPriceZar" to offer.promoPriceZar.coerceIn(0L, 1_000_000L),
                        "tagline" to tagline,
                        "active" to offer.active,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "title" to title,
                        "botName" to botName,
                        "originalPriceZar" to offer.originalPriceZar.coerceIn(0L, 1_000_000L),
                        "promoPriceZar" to offer.promoPriceZar.coerceIn(0L, 1_000_000L),
                        "tagline" to tagline,
                        "active" to offer.active,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun togglePromoOfferActive(offerId: String, active: Boolean): Result<Unit> {
        val path = "$COLLECTION_OFFERS/$offerId"
        return try {
            requireUserId()
            require(offerId.isNotBlank())
            db.collection(COLLECTION_OFFERS).document(offerId).update(
                mapOf("active" to active, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deletePromoOffer(offerId: String): Result<Unit> {
        val path = "$COLLECTION_OFFERS/$offerId"
        return try {
            requireUserId()
            require(offerId.isNotBlank())
            db.collection(COLLECTION_OFFERS).document(offerId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveSiteSettings(settings: SiteSettings): Result<Unit> {
        val path = "$COLLECTION_SETTINGS/$DOC_GLOBAL_SETTINGS"
        return try {
            val uid = requireUserId()
            val brokerUrl = settings.recommendedBrokerUrl.trim().take(500)
            require(brokerUrl.startsWith("https://") || brokerUrl.startsWith("http://")) {
                "Broker URL must start with https:// or http://"
            }
            val docRef = db.collection(COLLECTION_SETTINGS).document(DOC_GLOBAL_SETTINGS)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "recommendedBrokerUrl" to brokerUrl,
                        "recommendedBrokerHeading" to settings.recommendedBrokerHeading.trim().take(120),
                        "recommendedBrokerDescription" to settings.recommendedBrokerDescription.trim().take(500),
                        "blackFridayHeading" to settings.blackFridayHeading.trim().take(120),
                        "blackFridaySlogan" to settings.blackFridaySlogan.trim().take(160),
                        "blackFridayNotice" to settings.blackFridayNotice.trim().take(160),
                        "eaConnectHeading" to settings.eaConnectHeading.trim().take(120),
                        "eaConnectDescription" to settings.eaConnectDescription.trim().take(500),
                        "aboutHeading" to settings.aboutHeading.trim().take(120),
                        "aboutDescription" to settings.aboutDescription.trim().take(600),
                        "riskDisclaimer" to settings.riskDisclaimer.trim().take(600),
                        "updatedBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "recommendedBrokerUrl" to brokerUrl,
                        "recommendedBrokerHeading" to settings.recommendedBrokerHeading.trim().take(120),
                        "recommendedBrokerDescription" to settings.recommendedBrokerDescription.trim().take(500),
                        "blackFridayHeading" to settings.blackFridayHeading.trim().take(120),
                        "blackFridaySlogan" to settings.blackFridaySlogan.trim().take(160),
                        "blackFridayNotice" to settings.blackFridayNotice.trim().take(160),
                        "eaConnectHeading" to settings.eaConnectHeading.trim().take(120),
                        "eaConnectDescription" to settings.eaConnectDescription.trim().take(500),
                        "aboutHeading" to settings.aboutHeading.trim().take(120),
                        "aboutDescription" to settings.aboutDescription.trim().take(600),
                        "riskDisclaimer" to settings.riskDisclaimer.trim().take(600),
                        "updatedBy" to uid,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun logUserInquiry(
        botName: String,
        whatsappNumber: String,
        channel: InquiryChannel,
        priceZar: Long
    ): Result<Unit> {
        val docId = "inq_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "$COLLECTION_USERS/${auth.currentUser?.uid ?: "unauth"}/$SUBCOLLECTION_INQUIRIES/$docId"
        return try {
            val uid = requireUserId()
            val payload = mapOf(
                "userId" to uid,
                "botName" to botName.trim().ifBlank { "PraiseTraders" }.take(100),
                "whatsappNumber" to whatsappNumber.trim().ifBlank { "N/A" }.take(40),
                "channel" to channel.key,
                "priceZar" to priceZar.coerceIn(0L, 1_000_000L),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection(COLLECTION_USERS).document(uid)
                .collection(SUBCOLLECTION_INQUIRIES).document(docId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun persistPickedProductImage(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val imagesDir = File(context.filesDir, "bot_images").apply { mkdirs() }
                val fileName = "bot_img_${System.currentTimeMillis()}.jpg"
                val destFile = File(imagesDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IllegalStateException("Unable to read selected image.")
                Result.success(fileName)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
