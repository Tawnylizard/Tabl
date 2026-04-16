package com.app.tabl.data.local.dao

import androidx.room.*
import com.app.tabl.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications WHERE is_active = 1 ORDER BY created_at ASC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY created_at ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Delete
    suspend fun delete(medication: MedicationEntity)

    @Query("UPDATE medications SET is_active = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("UPDATE medications SET stock_count = stock_count - 1 WHERE id = :id AND stock_count > 0")
    suspend fun decrementStock(id: Long)

    @Query("UPDATE medications SET stock_count = :count WHERE id = :id")
    suspend fun updateStock(id: Long, count: Int)
}
