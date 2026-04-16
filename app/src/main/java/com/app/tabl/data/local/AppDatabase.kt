package com.app.tabl.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.tabl.data.local.converter.Converters
import com.app.tabl.data.local.dao.MedicationDao
import com.app.tabl.data.local.dao.MedicationLogDao
import com.app.tabl.data.local.dao.ScheduleDao
import com.app.tabl.data.local.entity.MedicationEntity
import com.app.tabl.data.local.entity.MedicationLogEntity
import com.app.tabl.data.local.entity.ScheduleEntity

@Database(
    entities = [MedicationEntity::class, ScheduleEntity::class, MedicationLogEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun medicationLogDao(): MedicationLogDao
}
