package dev.dunor.app.arXiv

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

class SettingsFontSerializer: Serializer<AppFontSettings> {
  override val defaultValue: AppFontSettings
    get() = AppFontSettings.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): AppFontSettings {
    try {
      return AppFontSettings.parseFrom(input)
    } catch (e: InvalidProtocolBufferException) {
      throw CorruptionException("Can't read proto.", e)
    }
  }

  override suspend fun writeTo(t: AppFontSettings, output: OutputStream) =
    t.writeTo(output)
}

val Context.appFontSettingsDataStore: DataStore<AppFontSettings> by dataStore(
  fileName = "app_font_setting.pb",
  serializer = SettingsFontSerializer()
)