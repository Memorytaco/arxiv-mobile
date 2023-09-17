package dev.dunor.app.arXiv.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.dunor.app.arXiv.R
import dev.dunor.app.arXiv.page.ArxivPage
import dev.dunor.app.arXiv.ui.theme.ArXivTheme

class AppMain : ComponentActivity() {
  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ArXivTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          App()
        }
      }
    }
  }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
  val context = LocalContext.current
  var expandMenu by remember { mutableStateOf(false) }
  var showAbout by remember {
    mutableStateOf(false)
  }

  @Composable
  fun OptionDropdownMenu(expanded: Boolean) {
    DropdownMenu(expanded = expanded, onDismissRequest = { expandMenu = false }) {
      DropdownMenuItem(
        text = { Text(text = "About arXiv App") },
        onClick = { showAbout = true },
        leadingIcon = { Icon(Icons.Sharp.Info, "application information") })
//      DropdownMenuItem(
//        text = { Text(text = "About Author") },
//        onClick = { /*TODO*/ },
//        leadingIcon = { Icon(Icons.Sharp.Info, "author information") })
      DropdownMenuItem(
        text = { Text(text = "Preference") },
        onClick = {
          context.startActivity(Intent(context, ArxivPreferenceActivity::class.java))
        },
        leadingIcon = { Icon(Icons.Sharp.Settings, "setting") })
    }
  }


  Scaffold(topBar = {
    CenterAlignedTopAppBar(
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary
      ),
      title = { Text(text = "Arxiv Mobile", color = MaterialTheme.colorScheme.onPrimaryContainer) },
      actions = {
        Box {
          IconButton(onClick = { expandMenu = true }) {
            AnimatedContent(targetState = expandMenu, label = "animation expand and collapse") {
              Icon(if (it) Icons.Rounded.Close else Icons.Rounded.Menu, "Option Menu")
            }

          }
          OptionDropdownMenu(expanded = expandMenu)
        }
      },
      navigationIcon = {
        Icon(Icons.Rounded.Home, null)
      })

  }) {
    ArxivPage(modifier = Modifier.padding(it))
    AnimatedVisibility(visible = showAbout) {
      Dialog(onDismissRequest = { showAbout = false }) {
        Card(
          modifier = Modifier
            .width(375.dp)
            .height(375.dp)
        ) {
          Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(text = stringResource(id = R.string.about_text), modifier = Modifier.padding(30.dp))
          }
        }
      }
    }
  }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun AppPreview() {
  ArXivTheme {
    App()
  }
}