package dev.dunor.app.arXiv

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApplication: Application() {
  override fun onCreate() {
    super.onCreate()
    DynamicColors.applyToActivitiesIfAvailable(this, R.style.AppTheme_Overlay)
  }
}