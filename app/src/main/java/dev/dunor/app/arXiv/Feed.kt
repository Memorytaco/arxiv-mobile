package dev.dunor.app.arXiv

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "feed")
data class Feed(
  @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "feed_id") val feedId: Long,
  @ColumnInfo(name = "title") var title: String,
  @ColumnInfo(name = "short_title") var shortTitle: String,
  @ColumnInfo(name = "url") val url: String,
  @ColumnInfo(name = "count") var count: Int,
  @ColumnInfo(name = "unread") val unread: Int,
)

data class FeedData(
  @ColumnInfo(name = "title") var title: String,
  @ColumnInfo(name = "short_title") var shortTitle: String,
  @ColumnInfo(name = "url") val url: String,
  @ColumnInfo(name = "count") val count: Int,
  @ColumnInfo(name = "unread") val unread: Int,
)

data class FeedID(@PrimaryKey @ColumnInfo(name = "feed_id") val feedID: Long)

@Dao
interface OperateFeed {
  companion object {
    private const val FEED_TABLE = "feed"
  }

  @Query("SELECT * from $FEED_TABLE")
  fun allFeeds(): List<Feed>

  @Update
  fun updateFeed(feed: Feed): Int

  @Insert
  fun insertFeed(feed: Feed): Long

  @Insert(entity = Feed::class)
  fun insertFeedData(data: FeedData): Long

  @Delete
  fun delete(feed: Feed): Int

  @Delete(entity = Feed::class)
  fun deleteByID(id: FeedID): Int

}