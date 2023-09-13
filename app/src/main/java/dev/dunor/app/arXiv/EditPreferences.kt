package dev.dunor.app.arXiv

import android.os.Bundle
import android.preference.PreferenceActivity

class EditPreferences : PreferenceActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }
}
