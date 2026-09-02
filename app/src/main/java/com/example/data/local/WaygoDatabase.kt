package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        Place::class,
        UserProfile::class,
        Badge::class,
        Review::class,
        UserCollection::class,
        CollectionItem::class,
        XPTransaction::class,
        NotificationItem::class,
        ReportItem::class,
        BusinessAccount::class,
        VisitedPlaceLog::class,
        SocialFollow::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WaygoDatabase : RoomDatabase() {
    abstract fun waygoDao(): WaygoDao

    companion object {
        @Volatile
        private var INSTANCE: WaygoDatabase? = null

        fun getInstance(context: Context): WaygoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaygoDatabase::class.java,
                    "waygo_app.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
