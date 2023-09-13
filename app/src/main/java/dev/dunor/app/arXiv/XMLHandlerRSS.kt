/*
    arXiv mobile - a Free arXiv app for android
    http://code.google.com/p/arxiv-mobile/

    Copyright (C) 2010 Jack Deslippe

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */
package dev.dunor.app.arXiv

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler


class XMLHandlerRSS : DefaultHandler() {
    // Fields
    private var in_items = false
    private var in_item = false
    private var in_title = false
    private var in_link = false
    private var in_date = false
    private var in_description = false
    private var in_dccreator = false
    var icount = 0
    var numItems = 0
    var date = ""
    var descriptions: Array<String?> = arrayOf()
    var titles: Array<String?> = arrayOf()
    var links: Array<String?> = arrayOf()
    var creators: Array<String?> = arrayOf()

    // Methods
    // Gets be called on the following structure: <tag>characters</tag>
    override fun characters(ch: CharArray, start: Int, length: Int) {
        if (in_items) {
        } else if (in_item) {
            if (in_description) {
                descriptions[icount] += String(ch, start, length)
            } else if (in_title) {
                titles[icount] += String(ch, start, length)
            } else if (in_link) {
                links[icount] += String(ch, start, length)
            } else if (in_dccreator) {
                creators[icount] += String(ch, start, length)
            }
        } else {
            if (in_date) {
                date += String(ch, start, length)
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
        if (localName == "items") {
            in_items = false
            // JRD Allocate space for string arrays
            titles = arrayOfNulls(numItems)
            creators = arrayOfNulls(numItems)
            links = arrayOfNulls(numItems)
            descriptions = arrayOfNulls(numItems)
        } else if (localName == "item") {
            in_item = false
            icount++
        } else if (localName == "title") {
            in_title = false
        } else if (localName == "link") {
            in_link = false
        } else if (localName == "date") {
            in_date = false
        } else if (localName == "description") {
            in_description = false
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
        if (localName == "items") {
            in_items = true
        } else if (localName == "item") {
            in_item = true
            titles[icount] = ""
            creators[icount] = ""
            links[icount] = ""
            descriptions[icount] = ""
        } else if (localName == "title") {
            in_title = true
        } else if (localName == "link") {
            in_link = true
        } else if (localName == "creator") {
            in_dccreator = true
        } else if (localName == "description") {
            in_description = true
        } else if (localName == "date") {
            in_date = true
        } else if (localName == "li") {
            numItems++
        }
    }
}
