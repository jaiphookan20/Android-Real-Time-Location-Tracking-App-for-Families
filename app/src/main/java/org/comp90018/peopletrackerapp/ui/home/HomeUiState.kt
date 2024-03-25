package org.comp90018.peopletrackerapp.ui.home

import org.comp90018.peopletrackerapp.models.Circle

data class HomeUiState (
    var circles: List<Circle> = emptyList()
)