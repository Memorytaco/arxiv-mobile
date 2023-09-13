package dev.dunor.app.arXiv

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ArxivComponent(modifier: Modifier = Modifier) {

  val navController = rememberNavController()

  Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
    NavHost(navController = navController, "category", modifier.fillMaxHeight()) {
      composable("category") { ArxivCategoryComponent(navController = navController) }
      composable("category_detail/{main}/{subcategory}", arguments = listOf(
        navArgument("main") {
          type = NavType.EnumType(ArxivRepository.ArxivCategory::class.java)
        },
        navArgument("subcategory") { type = NavType.StringType }
      )) {
        ArxivCategoryDetail(
          navController = navController,
          it.arguments!!.getSerializable("main", ArxivRepository.ArxivCategory::class.java)!!,
          it.arguments!!.getString("subcategory")!!
        )
      }
      composable("article") {}
    }
  }
}


// used to display a list of categories for arxiv
@Composable
fun ArxivCategoryComponent(navController: NavController) {
  val repository by remember { mutableStateOf(ArxivRepository()) }

  @Composable
  fun EntryCategory(category: ArxivRepository.ArxivCategory, expand: Boolean) {
    var localExpand by remember {
      mutableStateOf(expand)
    }
    Column {

      // header, used to show main category and an indicator button
      Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable {
            localExpand = !localExpand
          }
          .fillMaxWidth()) {
        Text(
          text = category.category,
          modifier = Modifier
            .weight(9.0F)
            .padding(start = 5.dp),
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          style = MaterialTheme.typography.headlineMedium,
        )
        Icon(
          if (localExpand) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
          null,
          modifier = Modifier
            .padding(10.dp, 0.dp)
            .weight(1.0F),
          tint = MaterialTheme.colorScheme.onSecondaryContainer
        )

      }

      AnimatedVisibility(visible = localExpand) {
        Column {
          repository.getSubCategories(category).forEach {
            Column(modifier = Modifier
              .clickable {
                navController.navigate("category_detail/${category}/${it.category}")
              }
              .padding(bottom = 10.dp)) {
              Text(text = it.category, style = MaterialTheme.typography.headlineSmall)
              if (it.description != null) Text(text = it.description)
            }
          }
        }
      }

    }
  }

  // Views
  Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
    repository.mainCategories.forEach {
      EntryCategory(category = it, false)
    }
  }

}

// start a search on a specific category
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArxivCategoryDetail(
  navController: NavController,
  mainCategory: ArxivRepository.ArxivCategory,
  subCategory: String
) {
  val itemsPerPage: Int by remember {
    mutableIntStateOf(30)
  }
  var isLoading by remember {
    mutableStateOf(true)
  }
  val startIndex by remember {
    mutableIntStateOf(0)
  }
  var categoryState: String? by remember {
    mutableStateOf(null)
  }
  var error: Exception? = null
  var arxivAtomEntries: List<ArxivAtomEntry>? by remember {
    mutableStateOf(null)
  }


  when (categoryState) {
    subCategory -> Unit
    else -> {
      categoryState = subCategory
      LaunchedEffect(categoryState, startIndex) {
        launch(Dispatchers.IO) {
          try {
            ArxivRepository().apply {
              val result = query(startIndex, itemsPerPage, paramFrom(mainCategory, subCategory))
              arxivAtomEntries = result.results
              isLoading = false
            }
          } catch (e: Exception) {
            error = e
            isLoading = false
          }
        }
      }
    }
  }

  when {
    isLoading -> Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      CircularProgressIndicator()
      Text(text = "Loading Category Data...")
    }
    error == null && !isLoading ->
      LazyColumn(content = {
        items(arxivAtomEntries ?: listOf()) {
          ElevatedCard(
            modifier = Modifier.padding(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
          ) {

            // header
            SelectionContainer {
              Text(
                text = it.title.replace('\n', ' '),
                modifier = Modifier.padding(0.dp, 3.dp),
                style = MaterialTheme.typography.headlineMedium
              )
            }

            // DOI information
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
              Row {
                Text(text = "DOI:")
                val context = LocalContext.current
                Surface(
                  color = if (it.doi == null) MaterialTheme.colorScheme.error else Color.Unspecified,
                  contentColor = if (it.doi == null) MaterialTheme.colorScheme.onError else Color.Unspecified,
                  shadowElevation = if (it.doi == null) 2.dp else 1.dp,
                  modifier = Modifier.clickable {
                    if (it.doi != null) {
                      val manager = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                      manager.setPrimaryClip(ClipData.newPlainText("DOI", it.doi))
                    }
                  }
                ) {
                  Text(
                    text = it.doi ?: "NONE",
                    modifier = Modifier.padding(horizontal = 5.dp),
                  )
                }
              }
            }

            @Composable
            fun CategoryView(modifier: Modifier = Modifier, category: String) {
              Surface(modifier = modifier, color = MaterialTheme.colorScheme.primary) {
                Row{
                  Text(text = category) }
              }
            }

            // category information
            Row(
              verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
              horizontalArrangement = Arrangement.End
            ) {
              val primaryCategory = it.primaryCategory
              Row(modifier = Modifier.padding(horizontal = 10.dp)) {
                Icon(Icons.Rounded.Star, "primary category icon", modifier = Modifier.padding(end = 2.dp))
                CategoryView(category = primaryCategory)
              }
              LazyRow {
                items(it.categories.filter { it != primaryCategory }) {
                  CategoryView(category = it, modifier = Modifier.padding(horizontal = 2.dp))
                }
              }
            }

            // updated time
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
              .padding(vertical = 3.dp)
              .fillMaxWidth()) {
              Row(modifier = Modifier.weight(1.0F)) {
                Icon(Icons.Rounded.Create, "published date icon", modifier = Modifier.padding(end = 3.dp))
                Text(text = SimpleDateFormat.getDateInstance().format(it.published))
              }
              Row(modifier = Modifier.weight(1.0F)) {
                Icon(Icons.Rounded.DateRange, "last updated date icon", modifier = Modifier.padding(end = 3.dp))
                Text(text = SimpleDateFormat.getDateInstance().format(it.updated))
              }
            }

            // authors
            Surface(
              color = MaterialTheme.colorScheme.tertiary,
              contentColor = contentColorFor(MaterialTheme.colorScheme.tertiary)
            ) {
              LazyRow(content = {
                items(it.authors) {
                    Icon(Icons.Rounded.Person, "author icon")
                    Text(text = it)
                }
              }, modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End)
            }

            // available resource button
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.End
            ) {
              val context = LocalContext.current
              it.links.filter { it.title != "self" }.forEach {
                Button(onClick = {
                  context.startActivity(
                    Intent(
                      Intent.ACTION_VIEW,
                      Uri.parse(it.url)
                    )
                  )
                }, shape = RectangleShape, elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp), modifier = Modifier.padding(horizontal = 5.dp)) {
                  Row {
                    Icon(Icons.Rounded.ArrowForward, it.title)
                    Text(it.title)
                  }
                }
              }
            }

            // Body, brief introduction
            SelectionContainer {
              Text(text = it.summary.replace('\n', ' '), modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      })
    else -> Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = error?.stackTraceToString() ?: "Something wrong with network",
        modifier = Modifier.padding(4.dp)
      )
    }
  }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun ArxivComponentPreview() {
  ArxivComponent()
}