package com.medlog.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medlog.app.data.local.converter.Converters
import com.medlog.app.data.local.dao.AppSettingDao
import com.medlog.app.data.local.dao.AppointmentDao
import com.medlog.app.data.local.dao.ClutterDao
import com.medlog.app.data.local.dao.ConditionDao
import com.medlog.app.data.local.dao.ConditionNoteDao
import com.medlog.app.data.local.dao.FileAttachmentDao
import com.medlog.app.data.local.dao.JournalDao
import com.medlog.app.data.local.dao.MedicationChangeDao
import com.medlog.app.data.local.dao.MedicationDao
import com.medlog.app.data.local.dao.MedicationLogDao
import com.medlog.app.data.local.dao.ProfileDao
import com.medlog.app.data.local.dao.SectionDao
import com.medlog.app.data.local.dao.SectionEntryDao
import com.medlog.app.data.local.entity.AppSettingEntity
import com.medlog.app.data.local.entity.AppointmentEntity
import com.medlog.app.data.local.entity.ClutterItemEntity
import com.medlog.app.data.local.entity.ConditionEntity
import com.medlog.app.data.local.entity.ConditionNoteEntity
import com.medlog.app.data.local.entity.FileAttachmentEntity
import com.medlog.app.data.local.entity.JournalEntryEntity
import com.medlog.app.data.local.entity.MedicationChangeEntity
import com.medlog.app.data.local.entity.MedicationEntity
import com.medlog.app.data.local.entity.MedicationLogEntity
import com.medlog.app.data.local.entity.ProfileEntity
import com.medlog.app.data.local.entity.SectionEntity
import com.medlog.app.data.local.entity.SectionEntryEntity

@Database(
    entities = [
        ProfileEntity::class,
        ConditionEntity::class,
        ConditionNoteEntity::class,
        MedicationEntity::class,
        MedicationLogEntity::class,
        MedicationChangeEntity::class,
        AppointmentEntity::class,
        FileAttachmentEntity::class,
        SectionEntity::class,
        SectionEntryEntity::class,
        ClutterItemEntity::class,
        JournalEntryEntity::class,
        AppSettingEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MedLogDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun conditionDao(): ConditionDao
    abstract fun conditionNoteDao(): ConditionNoteDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationLogDao(): MedicationLogDao
    abstract fun medicationChangeDao(): MedicationChangeDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun fileAttachmentDao(): FileAttachmentDao
    abstract fun sectionDao(): SectionDao
    abstract fun sectionEntryDao(): SectionEntryDao
    abstract fun clutterDao(): ClutterDao
    abstract fun journalDao(): JournalDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: MedLogDatabase? = null

        fun getInstance(context: Context): MedLogDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MedLogDatabase::class.java,
                    "medlog.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}