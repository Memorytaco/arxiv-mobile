package dev.dunor.app.arXiv.persistent

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.dunor.app.arXiv.data.Feed
import dev.dunor.app.arXiv.data.FeedData
import dev.dunor.app.arXiv.data.FeedID
import dev.dunor.app.arXiv.data.History
import dev.dunor.app.arXiv.data.HistoryData
import dev.dunor.app.arXiv.data.HistoryID
import dev.dunor.app.arXiv.data.OperateFeed
import dev.dunor.app.arXiv.data.OperateHistory
import dev.dunor.app.arXiv.data.appFontSettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ArxivPrivateStorage(val context: Context) {
    private val database: ArxivDatabase = Room.databaseBuilder(
            context = context,
            ArxivDatabase::class.java, name = DATABASE_NAME
    ).allowMainThreadQueries().build()

    fun close() = database.close()

    val feeds: List<Feed>
        get() = database.operateFeed().allFeeds()
    fun deleteFeed(feedID: Long): Boolean =
        database.operateFeed().deleteByID(FeedID(feedID)) == 1
    fun insertFeed(feed: FeedData): Boolean =
        database.operateFeed().insertFeedData(feed) != (-1).toLong()
    fun insertFeed(title: String, shortTitle: String, url: String, count: Int, unread: Int): Boolean =
        insertFeed(FeedData(title, shortTitle, url, count, unread))

    fun updateFeed(feed: Feed): Boolean =
        database.operateFeed().updateFeed(feed) == 1
    fun updateFeed(feedId: Long, title: String, shortTitle: String, url: String, count: Int, unread: Int): Boolean
        = updateFeed(Feed(feedId, title, shortTitle, url, count, unread))


    val history: List<History>
        get() = database.operateHistory().allHistory()
    fun deleteHistory(historyID: Long): Boolean =
        database.operateHistory().deleteByID(HistoryID(historyID)) == 1
    fun insertHistory(displayText: String, url: String): Boolean =
        database.operateHistory().insertHistory(HistoryData(displayText, url)) != (-1).toLong()


    val size: Int
        get() = runBlocking{ context.appFontSettingsDataStore.data.first().fontSize }

    suspend fun changeSize(size: Int): Boolean =
            context.appFontSettingsDataStore.updateData { fontSetting ->
                fontSetting.toBuilder().setFontSize(size).build()
            }.let { true }


    companion object DatabaseSetting {
        private const val DATABASE_NAME = "arXiv"
    }
}


@Database(entities = [Feed::class, History::class], version = 1)
abstract class ArxivDatabase : RoomDatabase() {
    abstract fun operateFeed() : OperateFeed
    abstract fun operateHistory() : OperateHistory
}



