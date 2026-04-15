package com.mobileclaw.app.runtime.persona

import kotlinx.coroutines.flow.Flow

interface PersonaRepository {
    val personaProfile: Flow<PersonaProfile>

    suspend fun getCurrentProfile(): PersonaProfile

    suspend fun updatePersona(
        transform: (PersonaProfile) -> PersonaProfile,
    ): PersonaProfile
}
