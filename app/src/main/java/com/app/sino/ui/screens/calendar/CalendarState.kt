
package com.app.sino.ui.screens.calendar

import com.app.sino.data.remote.dto.EventDto

data class CalendarState(
    val events: List<EventDto> = emptyList()
)
