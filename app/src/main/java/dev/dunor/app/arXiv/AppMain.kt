package dev.dunor.app.arXiv

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.Settings
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
  var expandMenu by remember { mutableStateOf(false) }
  
  @Composable
  fun OptionDropdownMenu(expanded: Boolean) {
    DropdownMenu(expanded = expanded, onDismissRequest = { expandMenu = false }) {
      DropdownMenuItem(
        text = { Text(text = "About Author") },
        onClick = { /*TODO*/ },
        leadingIcon = { Icon(Icons.Sharp.Info, "author information") })
      DropdownMenuItem(
        text = { Text(text = "Preference") },
        onClick = { /*TODO*/ },
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
    ArxivComponent(modifier = Modifier.padding(it))
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