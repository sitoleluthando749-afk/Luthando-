package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Announcement
import com.example.data.model.DefaultCatalog
import com.example.data.model.InquiryChannel
import com.example.data.model.PromoOffer
import com.example.data.model.SiteSettings
import com.example.data.model.TradingBot
import com.example.data.model.UiState
import com.example.data.model.UserInquiry
import com.example.data.repository.PraiseTradersRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PraiseTradersViewModel(
    private val repository: PraiseTradersRepository,
    val currentUserId: String,
    val currentUserEmail: String?,
    private val isEmailVerified: Boolean
) : ViewModel() {

    private companion object {
        const val TAG = "PraiseTradersVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }

    val isAdmin: StateFlow<Boolean> = repository
        .observeIsAdmin(currentUserId, currentUserEmail, isEmailVerified)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = currentUserEmail.equals(
                PraiseTradersRepository.BOOTSTRAPPED_ADMIN_EMAIL,
                ignoreCase = true
            ) && isEmailVerified
        )

    private val settingsFlow: Flow<Pair<SiteSettings, Boolean>> =
        repository.observeSiteSettings()

    val siteSettingsState: StateFlow<UiState<SiteSettings>> = settingsFlow
        .map<Pair<SiteSettings, Boolean>, UiState<SiteSettings>> { (settings, _) ->
            UiState.Success(settings)
        }
        .catch { error ->
            val fsErr = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing site settings", fsErr)
            emit(UiState.Error(fsErr.message ?: "Failed to load site settings"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(DefaultCatalog.defaultSiteSettings)
        )

    val botsState: StateFlow<UiState<List<TradingBot>>> = isAdmin
        .flatMapLatest { admin ->
            combine(
                repository.observeBots(includeInactive = admin),
                settingsFlow
            ) { bots, (_, seededInFirestore) ->
                val resolved = if (bots.isEmpty() && !seededInFirestore) {
                    DefaultCatalog.defaultBots
                } else {
                    bots
                }
                UiState.Success(resolved) as UiState<List<TradingBot>>
            }
        }
        .catch { error ->
            val fsErr = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing trading bots", fsErr)
            emit(UiState.Error(fsErr.message ?: "Failed to load trading bots"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val announcementsState: StateFlow<UiState<List<Announcement>>> = isAdmin
        .flatMapLatest { admin ->
            combine(
                repository.observeAnnouncements(includeInactive = admin),
                settingsFlow
            ) { announcements, (_, seededInFirestore) ->
                val resolved = if (announcements.isEmpty() && !seededInFirestore) {
                    DefaultCatalog.defaultAnnouncements
                } else {
                    announcements
                }
                UiState.Success(resolved) as UiState<List<Announcement>>
            }
        }
        .catch { error ->
            val fsErr = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing announcements", fsErr)
            emit(UiState.Error(fsErr.message ?: "Failed to load announcements"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val promoOffersState: StateFlow<UiState<List<PromoOffer>>> = isAdmin
        .flatMapLatest { admin ->
            combine(
                repository.observePromoOffers(includeInactive = admin),
                settingsFlow
            ) { offers, (_, seededInFirestore) ->
                val resolved = if (offers.isEmpty() && !seededInFirestore) {
                    DefaultCatalog.defaultPromoOffers
                } else {
                    offers
                }
                UiState.Success(resolved) as UiState<List<PromoOffer>>
            }
        }
        .catch { error ->
            val fsErr = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing promo offers", fsErr)
            emit(UiState.Error(fsErr.message ?: "Failed to load promotional offers"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val inquiriesState: StateFlow<UiState<List<UserInquiry>>> = repository
        .observeUserInquiries(currentUserId)
        .map<List<UserInquiry>, UiState<List<UserInquiry>>> { UiState.Success(it) }
        .catch { error ->
            val fsErr = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing user inquiries", fsErr)
            emit(UiState.Error(fsErr.message ?: "Failed to load inquiries"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    private var hasAttemptedAdminSeed = false

    init {
        viewModelScope.launch {
            isAdmin.collect { admin ->
                if (admin && !hasAttemptedAdminSeed) {
                    hasAttemptedAdminSeed = true
                    repository.seedOfficialDefaultsIfNeeded()
                }
            }
        }
    }

    private fun unwrapFirestoreError(error: Throwable): Throwable {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        return firestoreError ?: error
    }

    fun seedOfficialDefaults() {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.seedOfficialDefaultsIfNeeded()
            _isBusy.value = false
            _snackbarMessages.tryEmit(
                if (result.isSuccess) "Official PraiseTraders catalog synced to cloud."
                else result.exceptionOrNull()?.localizedMessage ?: "Failed to sync defaults."
            )
        }
    }

    fun saveBot(bot: TradingBot, isNew: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveBot(bot, isNew)
            _isBusy.value = false
            if (result.isSuccess) {
                _snackbarMessages.tryEmit(if (isNew) "Trading bot added." else "Trading bot updated.")
                onDone()
            } else {
                _snackbarMessages.tryEmit(result.exceptionOrNull()?.localizedMessage ?: "Failed to save bot.")
            }
        }
    }

    fun toggleBotActive(botId: String, active: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleBotActive(botId, active)
            if (result.isFailure) {
                _snackbarMessages.tryEmit(result.exceptionOrNull()?.localizedMessage ?: "Failed to update status.")
            } else {
                _snackbarMessages.tryEmit(if (active) "Product activated." else "Product deactivated.")
            }
        }
    }

    fun deleteBot(botId: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteBot(botId)
            _isBusy.value = false
            _snackbarMessages.tryEmit(
                if (result.isSuccess) "Trading bot deleted."
                else result.exceptionOrNull()?.localizedMessage ?: "Failed to delete bot."
            )
        }
    }

    fun saveAnnouncement(announcement: Announcement, isNew: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveAnnouncement(announcement, isNew)
            _isBusy.value = false
            if (result.isSuccess) {
                _snackbarMessages.tryEmit(if (isNew) "Announcement published." else "Announcement updated.")
                onDone()
            } else {
                _snackbarMessages.tryEmit(result.exceptionOrNull()?.localizedMessage ?: "Failed to save announcement.")
            }
        }
    }

    fun toggleAnnouncementActive(id: String, active: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleAnnouncementActive(id, active)
            if (result.isFailure) {
                _snackbarMessages.tryEmit(result.exceptionOrNull()?.localizedMessage ?: "Failed to toggle announcement.")
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            val result = repository.deleteAnnouncement(id)
            _snackbarMessages.tryEmit(
                if (result.isSuccess) "Announcement removed."
                else result.exceptionOrNull()?.localizedMessage ?: "Failed to remove announcement."
            )
        }
    }

    fun savePromoOffer(offer: PromoOffer, isNew: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.savePromoOffer(offer, isNew)
            _isBusy.value = false
            if (result.isSuccess) {
                _snackbarMessages.tryEmit(if (isNew) "Promotional offer added." else "Promotional offer updated.")
                onDone()
            } else {
                _snackbarMessages.tryEmit(result.exceptionOrNull()?.localizedMessage ?: "Failed to save offer.")
            }
        }
    }

    fun togglePromoOfferActive(id: String, active: Boolean) {
        viewModelScope.launch {
            val result = repository.togglePromoOfferActive(id, active)
            if (result.isFailure) {
                _snackbarMessages.tryEmit(result.exceptionOrNull()?.localizedMessage ?: "Failed to update offer.")
            }
        }
    }

    fun deletePromoOffer(id: String) {
        viewModelScope.launch {
            val result = repository.deletePromoOffer(id)
            _snackbarMessages.tryEmit(
                if (result.isSuccess) "Promotional offer removed."
                else result.exceptionOrNull()?.localizedMessage ?: "Failed to remove offer."
            )
        }
    }

    fun saveSiteSettings(settings: SiteSettings) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveSiteSettings(settings)
            _isBusy.value = false
            _snackbarMessages.tryEmit(
                if (result.isSuccess) "Site & broker settings saved."
                else result.exceptionOrNull()?.localizedMessage ?: "Failed to save settings."
            )
        }
    }

    fun recordInquiry(botName: String, whatsappNumber: String, channel: InquiryChannel, priceZar: Long) {
        viewModelScope.launch {
            repository.logUserInquiry(botName, whatsappNumber, channel, priceZar)
        }
    }

    suspend fun importPickedImage(context: Context, uri: Uri): Result<String> {
        return repository.persistPickedProductImage(context, uri)
    }
}
