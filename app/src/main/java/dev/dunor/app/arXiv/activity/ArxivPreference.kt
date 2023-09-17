package dev.dunor.app.arXiv.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dev.dunor.app.arXiv.R
import dev.dunor.app.arXiv.data.preferenceArxivDataStore
import dev.dunor.app.arXiv.ui.theme.ArXivTheme
import dev.dunor.app.arXiv.util.findActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ArxivPreferenceActivity: ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycleScope.launch {
      applicationContext.preferenceArxivDataStore.data.first()
    }
    setContent {
      ArXivTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          ArxivPreference()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ArxivPreference() {
  val context = LocalContext.current

  // if true, we use Arxiv api to fetch article source
  var useApiorNot by remember {
    mutableStateOf(runBlocking { context.preferenceArxivDataStore.data.first().useApi })
  }

  LaunchedEffect(key1 = useApiorNot, block = {
    launch(Dispatchers.IO) {
      context.preferenceArxivDataStore.updateData {
        it.toBuilder().setUseApi(useApiorNot).build()
      }
    }
  })

  // toggleable entry component
  @Composable
  fun ToggleEntry(title: String, description: String, toggle: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(modifier = Modifier
      .fillMaxWidth()
      .padding(10.dp)) {
      Text(text = title, style = MaterialTheme.typography.headlineSmall)
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = description, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1F))
        Switch(checked = toggle, onCheckedChange = onCheckedChange)
      }
    }
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text(text = "Arxiv Preference") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
          IconButton(onClick = {
            context.findActivity().finish()
          }) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "return from preference")
          }
        }
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(it),
    ) {
      ToggleEntry("Use Arxiv Api as article source", stringResource(id = R.string.preference_arxiv_desc_article_source), useApiorNot) {it ->
        useApiorNot = it
      }
    }
  }
}

