package dev.dunor.app.arXiv.page

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.dunor.app.arXiv.repository.ArxivRepository
import dev.dunor.app.arXiv.page.arxiv.ArxivCategoryDetail

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ArxivPage(modifier: Modifier = Modifier) {

  val navController = rememberNavController()

  Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
    NavHost(navController = navController, "category", modifier.fillMaxHeight()) {
      composable("category") {
        ArxivCategoryComponent { mainCategory, subCategory ->
          navController.navigate("category_detail/$mainCategory/$subCategory")
        }
      }
      composable("category_detail/{main}/{subcategory}", arguments = listOf(
        navArgument("main") {
          type = NavType.EnumType(ArxivRepository.ArxivCategory::class.java)
        },
        navArgument("subcategory") { type = NavType.StringType }
      )) {
        ArxivCategoryDetail(
          it.arguments!!.getSerializable("main", ArxivRepository.ArxivCategory::class.java)!!,
          it.arguments!!.getString("subcategory")!!
        )
      }
    }
  }
}


// used to display a list of categories for arxiv
@Composable
fun ArxivCategoryComponent(gotoCategoryDetail: ((ArxivRepository.ArxivCategory, String) -> Unit)) {
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
                gotoCategoryDetail(category, it.category)
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


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun ArxivComponentPreview() {
  ArxivPage()
}