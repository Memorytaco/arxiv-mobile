package dev.dunor.app.arXiv.util

import android.annotation.SuppressLint
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.START_TAG
import org.xmlpull.v1.XmlPullParser.END_TAG
import org.xmlpull.v1.XmlPullParser.END_DOCUMENT
import org.xmlpull.v1.XmlPullParserException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

class ArxivAtomSearchParser(private val inputStream: InputStream) {
  private val parser = Xml.newPullParser()
  var totalResults: Int = 0
  var startIndex: Int = 0
  var itemsPerPage: Int = 0
  // when does this atom list get updated
  var updatedTime: Date = Date()  // default to current time
  var query: String = ""
  var queryId: String = ""
  lateinit var queryLink: ArxivAtomLink
  val entries = mutableListOf<ArxivAtomEntry>()

  init {
    parser.apply {
      setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
      setInput(inputStream, null)
      nextTag()
      require(START_TAG, null, "feed")
      // start parsing feed
      readFeed()
    }
  }

  private fun readFeed() {
    parser.apply {
      require(START_TAG, null, "feed")
      nextTag()
      while ((eventType != END_TAG && eventType != END_DOCUMENT) || name != "feed") {
        when (name) {
          "link" -> queryLink = readLink()
          "title" -> query = readText("title")
          "id" -> queryId = readText("id")
          "updated" -> updatedTime = readTime("updated", FeedDateFormat)
          "totalResults" -> totalResults = readInt("totalResults")
          "startIndex" -> startIndex = readInt("startIndex")
          "itemsPerPage" -> itemsPerPage = readInt("itemsPerPage")
          "entry" -> entries += readEntry()
          else -> skip()
        }
        nextTag()
      }
      require(END_TAG, null, "feed")
    }
  }


  private fun readEntry(): ArxivAtomEntry {
    var id = ""
    var updated = Date()
    var published = Date()
    var title = ""
    var summary = ""
    val authors = mutableListOf<String>()
    var doi : String? = null
    var primaryCategory = ""
    val links = mutableListOf<ArxivAtomLink>()
    val categories = mutableListOf<String>()
    parser.apply {
      require(START_TAG, null, "entry")
      nextTag()
      while (eventType != END_TAG || name != "entry") {
        when (name) {
          "id" -> id = readText("id")
          "updated" -> updated = readTime("updated", EntryDateFormat)
          "published" -> published = readTime("published", EntryDateFormat)
          "title" -> title = readText("title")
          "summary" -> summary = readText("summary")
          "author" -> authors += readAuthor()
          "doi" -> doi = readText("doi")
          "link" -> links += readLink()
          "category" -> categories += readCategory("category")
          "primary_category" -> primaryCategory = readCategory("primary_category")
          "entry" -> break
          else -> skip()
        }
        nextTag()
      }
      require(END_TAG, null, "entry")
    }
    return ArxivAtomEntry(id, title, primaryCategory, categories.toList(), summary, authors.toList(), updated, published, links.toList(), doi)
  }

  // used to parse totalResults, startIndex and ItemsPerPage
  private fun readInt(name: String): Int {
    parser.apply {
      require(START_TAG, null, name)
      val i = nextText().toInt()
      require(END_TAG, null, name)
      return i
    }
  }

  // skip will stop at an end tag
  @Throws(XmlPullParserException::class)
  private fun skip() {
    if (parser.eventType != START_TAG) {
      throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
      when (parser.next()) {
        END_TAG -> depth--
        START_TAG -> depth++
      }
    }
  }

  private fun readCategory(name: String): String {
    parser.apply {
      require(START_TAG, null, name)
      val category = getAttributeValue(null, "term") ?: ""
      nextTag()
      require(END_TAG, null, name)
      return category
    }
  }

  private fun readAuthor(): String {
    var author = ""
    parser.apply {
      require(START_TAG, null, "author")
      nextTag()
      while (eventType != END_TAG || name != "author") {
        when (name) {
          "name" -> author = readText("name")
          else -> skip()
        }
        nextTag()
      }
      require(END_TAG, null, "author")
      return author
    }
  }

  private fun readText(name: String) : String {
    parser.apply {
      require(START_TAG, null, name)
      val text = nextText()
      require(END_TAG, null, name)
      return text
    }
  }

  @SuppressLint("SimpleDateFormat")
  private fun readTime(name: String, format: String): Date {
    parser.apply {
      require(START_TAG, null, name)
      val date = SimpleDateFormat(format).parse(nextText())!!
      require(END_TAG, null, name)
      return date
    }
  }

  private fun readLink(): ArxivAtomLink {
    parser.apply {
      require(START_TAG, null, "link")
      if (attributeCount == -1) throw XmlPullParserException("Expect attributes in link tag")
      val title = getAttributeValue(null, "title") ?: (if (getAttributeValue(namespace, "rel") == "self") "self" else "abs")
      val url = getAttributeValue(null, "href").orEmpty()
      nextTag()
      require(END_TAG, null, "link")
      return ArxivAtomLink(title, url)
    }
  }

  companion object {
    const val EntryDateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    const val FeedDateFormat = "yyyy-MM-dd'T'HH:mm:ss-HH:mm"
  }

}

data class ArxivAtomLink(val title: String, val url: String)
data class ArxivAtomEntry(
  val id: String,
  val title: String,
  val primaryCategory: String,
  val categories: List<String>,
  val summary: String,
  val authors: List<String>,
  val updated: Date,
  val published: Date,
  val links: List<ArxivAtomLink> = listOf(),
  val doi: String? = null,
)