package dev.dunor.app.arXiv

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler


class XMLHandlerSearch : DefaultHandler() {
  // Fields
  private var in_totalresults = false
  private var in_item = false
  private var in_title = false
  private var in_link = false
  private var in_updated_date = false
  private var in_published_date = false
  private var in_description = false
  private var in_dccreator = false
  private var vFirstCategory = true
  private var icount = 0
  private var ntotal = ""

  var numItems = 0
  var numTotalItems = 0
  var updatedDates: Array<String?> = arrayOf()
  var publishedDates: Array<String?> = arrayOf()
  var descriptions: Array<String?> = arrayOf()
  var titles: Array<String?> = arrayOf()
  var links: Array<String?> = arrayOf()
  var creators: Array<String?> = arrayOf()
  var categories: Array<String?> = arrayOf()

  // Methods
  // Gets be called on the following structure: <tag>characters</tag>
  override fun characters(ch: CharArray, start: Int, length: Int) {
    if (in_item) {
      if (in_description) {
        descriptions[icount] += String(ch, start, length)
      } else if (in_title) {
        titles[icount] += String(ch, start, length)
      } else if (in_link) {
        links[icount] += String(ch, start, length)
      } else if (in_updated_date) {
        updatedDates[icount] += String(ch, start, length)
      } else if (in_published_date) {
        publishedDates[icount] += String(ch, start, length)
      } else if (in_dccreator) {
        creators[icount] += String(ch, start, length)
      }
    } else {
      if (in_totalresults) {
        ntotal += String(ch, start, length)
      }
    }
  }

  @Throws(SAXException::class)
  override fun endDocument() {
    // Nothing to do
  }

  // Gets be called on closing tags like: </tag>
  @Throws(SAXException::class)
  override fun endElement(namespaceURI: String, localName: String, qName: String) {
    when (localName) {
      "updated" -> {
        in_updated_date = false
      }
      "published" -> {
        in_published_date = false
      }
      "entry" -> {
        in_item = false
        icount++
        vFirstCategory = true
        numItems = icount
      }
      "totalResults" -> {
        in_totalresults = false
        numTotalItems = ntotal.toInt()
      }
      "title" -> {
        in_title = false
      }
      "id" -> {
        in_link = false
      }
      "name" -> {
        in_dccreator = false
        creators[icount] = creators[icount] + "</a>"
      }
      "summary" -> {
        in_description = false
      }
    }
  }

  @Throws(SAXException::class)
  override fun startDocument() {
    // Nothing to do
  }

  // Gets be called on opening tags like: <tag>
  @Throws(SAXException::class)
  override fun startElement(namespaceURI: String, localName: String,
                            qName: String, atts: Attributes) {
    when (localName) {
      "updated" -> in_updated_date = true
      "published" -> in_published_date = true
      "entry" -> {
        in_item = true
        titles[icount] = ""
        updatedDates[icount] = ""
        publishedDates[icount] = ""
        creators[icount] = ""
        links[icount] = ""
        descriptions[icount] = ""
      }
      "totalResults" -> {
        in_totalresults = true
        updatedDates = arrayOfNulls(30)
        publishedDates = arrayOfNulls(30)
        descriptions = arrayOfNulls(30)
        categories = arrayOfNulls(30)
        titles = arrayOfNulls(30)
        links = arrayOfNulls(30)
        creators = arrayOfNulls(30)
      }
      "title" -> in_title = true
      "id" -> in_link = true
      "category" -> {
        if (vFirstCategory) {
          categories[icount] = atts.getValue("term")
          vFirstCategory = false
        }
      }
      "name" -> {
        in_dccreator = true
        creators[icount] = creators[icount] + "<a>"
      }
      "summary" -> in_description = true
//      else -> println("Encountered tag: $localName , $namespaceURI , $qName")
    }
  }
}
