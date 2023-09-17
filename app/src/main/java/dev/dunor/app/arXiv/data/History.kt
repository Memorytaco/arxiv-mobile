package dev.dunor.app.arXiv.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query


@Entity(tableName = "history")
data class History(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "history_id") val historyID: Long,
  @ColumnInfo(name = "display_text") val displayText: String,
  @ColumnInfo(name = "url") val url: String,
)

data class HistoryID(@PrimaryKey @ColumnInfo(name = "history_id") val historyID: Long)
data class HistoryData(
  @ColumnInfo(name = "display_text") val displayText: String,
  @ColumnInfo(name = "url") val url: String,
)

@Dao
interface OperateHistory {
  companion object {
    private const val HISTORY_TABLE = "history"
  }

  @Query("SELECT * from $HISTORY_TABLE")
  fun allHistory(): List<History>

  @Insert(entity = History::class)
  fun insertHistory(data: HistoryData): Long

  @Delete(entity = History::class)
  fun deleteByID(historyID: HistoryID): Int
}