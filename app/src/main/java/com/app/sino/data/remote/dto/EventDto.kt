
package com.app.sino.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalTime

data class EventDto(
    @SerializedName("id_events") val idEvents: Int?,
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("id_course") val idCourse: Int,
    @SerializedName("dsc_title") val dscTitle: String,
    @SerializedName("dsc_description") val dscDescription: String?,
    @SerializedName("event_date") val eventDate: LocalDate,
    @SerializedName("event_time") val eventTime: LocalTime?,
    @SerializedName("type_event") val typeEvent: String,
    @SerializedName("status") val status: String?
)
