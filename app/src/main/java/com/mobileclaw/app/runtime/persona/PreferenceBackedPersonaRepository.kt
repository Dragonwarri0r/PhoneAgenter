package com.mobileclaw.app.runtime.persona

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mobileclaw.app.R
import com.mobileclaw.app.runtime.strings.AppStrings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class PreferenceBackedPersonaRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val appStrings: AppStrings,
) : PersonaRepository {

    override val personaProfile: Flow<PersonaProfile> = dataStore.data.map(::toPersonaProfile)

    override suspend fun getCurrentProfile(): PersonaProfile = personaProfile.first()

    override suspend fun updatePersona(
        transform: (PersonaProfile) -> PersonaProfile,
    ): PersonaProfile {
        val updatedProfile = transform(getCurrentProfile()).copy(
            updatedAtEpochMillis = System.currentTimeMillis(),
        )
        dataStore.edit { prefs ->
            prefs[Keys.PERSONA_ID] = updatedProfile.personaId
            prefs[Keys.DISPLAY_NAME] = updatedProfile.displayName
            prefs[Keys.VERBOSITY] = updatedProfile.verbosity.name
            prefs[Keys.WARMTH] = updatedProfile.warmth.name
            prefs[Keys.CONFIRMATION_STYLE] = updatedProfile.confirmationStyle.name
            prefs[Keys.AVOID_OVERCOMMITMENT] = updatedProfile.avoidOvercommitment
            prefs[Keys.ASK_BEFORE_SCHEDULING] = updatedProfile.askBeforeScheduling
            prefs[Keys.UPDATED_AT] = updatedProfile.updatedAtEpochMillis
        }
        return updatedProfile
    }

    private fun toPersonaProfile(preferences: Preferences): PersonaProfile {
        return PersonaProfile(
            personaId = preferences[Keys.PERSONA_ID] ?: PersonaProfile.DEFAULT_PERSONA_ID,
            displayName = preferences[Keys.DISPLAY_NAME] ?: appStrings.get(R.string.persona_default_self),
            verbosity = preferences[Keys.VERBOSITY]
                ?.let(PersonaVerbosity::valueOf)
                ?: PersonaFixtures.defaultProfile().verbosity,
            warmth = preferences[Keys.WARMTH]
                ?.let(PersonaWarmth::valueOf)
                ?: PersonaFixtures.defaultProfile().warmth,
            confirmationStyle = preferences[Keys.CONFIRMATION_STYLE]
                ?.let(ConfirmationStyle::valueOf)
                ?: PersonaFixtures.defaultProfile().confirmationStyle,
            avoidOvercommitment = preferences[Keys.AVOID_OVERCOMMITMENT]
                ?: PersonaFixtures.defaultProfile().avoidOvercommitment,
            askBeforeScheduling = preferences[Keys.ASK_BEFORE_SCHEDULING]
                ?: PersonaFixtures.defaultProfile().askBeforeScheduling,
            updatedAtEpochMillis = preferences[Keys.UPDATED_AT]
                ?: PersonaFixtures.defaultProfile().updatedAtEpochMillis,
        )
    }

    private object Keys {
        val PERSONA_ID = stringPreferencesKey("persona.id")
        val DISPLAY_NAME = stringPreferencesKey("persona.display_name")
        val VERBOSITY = stringPreferencesKey("persona.verbosity")
        val WARMTH = stringPreferencesKey("persona.warmth")
        val CONFIRMATION_STYLE = stringPreferencesKey("persona.confirmation_style")
        val AVOID_OVERCOMMITMENT = booleanPreferencesKey("persona.avoid_overcommitment")
        val ASK_BEFORE_SCHEDULING = booleanPreferencesKey("persona.ask_before_scheduling")
        val UPDATED_AT = longPreferencesKey("persona.updated_at")
    }
}
