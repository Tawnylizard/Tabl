package com.app.tabl.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromDaysOfWeek(days: Set<Int>): String = Json.encodeToString(days.toList())

    @TypeConverter
    fun toDaysOfWeek(json: String): Set<Int> = Json.decodeFromString<List<Int>>(json).toSet()

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
