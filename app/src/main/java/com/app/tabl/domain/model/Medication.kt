package com.app.tabl.domain.model

data class Medication(
    val id: Long = 0,
    val name: String,
    val dose: String? = null,
    val colorIndex: Int = 0,
    val stockCount: Int? = null,
    val stockThreshold: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
