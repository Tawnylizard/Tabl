package com.app.tabl.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.tabl.domain.model.Medication

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dose: String?,
    @ColumnInfo(name = "color_index") val colorIndex: Int = 0,
    @ColumnInfo(name = "stock_count") val stockCount: Int?,
    @ColumnInfo(name = "stock_threshold") val stockThreshold: Int?,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Medication(id, name, dose, colorIndex, stockCount, stockThreshold, isActive, createdAt)
}

fun Medication.toEntity() = MedicationEntity(id, name, dose, colorIndex, stockCount, stockThreshold, isActive, createdAt)
