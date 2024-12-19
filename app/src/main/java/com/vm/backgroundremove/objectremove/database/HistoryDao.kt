package com.vm.backgroundremove.objectremove.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcess(processModel: HistoryModel): Long

    @Update
    suspend fun updateProcess(processModel: HistoryModel)

    @Query("select * from HistoryModel")
    fun getAllProcess(): Flow<List<HistoryModel>>

    @Delete
    suspend fun deleteProcess(processModel: HistoryModel)

    @Query("UPDATE HistoryModel SET process = :process WHERE id = :id")
    suspend fun updateProcess(id: Long, process: Int)

    @Query("UPDATE HistoryModel SET imageResult = :imageResult WHERE id = :id")
    suspend fun updateImage(id: Long, imageResult: String)

    @Query("UPDATE HistoryModel SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE HistoryModel SET type = :type WHERE id = :id")
    suspend fun updateType(id: Long, type: String)

    @Query("UPDATE HistoryModel SET other = :other WHERE id = :id")
    suspend fun updateOther(id: Long, other: String)

    @Query("SELECT COUNT(name) FROM HistoryModel")
    fun getRowCount() : Int

    @Query("SELECT COUNT(name) FROM HistoryModel where type like 'remove_obj_by_text' OR type like 'remove_obj_by_list'")
    fun getRowObjectRemoveCount() : Int

    @Query("SELECT COUNT(name) FROM HistoryModel where type like 'remove_background'")
    fun getRowRemoveBGCount() : Int

    @Query("select * from HistoryModel where id like :id")
    suspend fun getProcessByID(id: Long) : HistoryModel
}