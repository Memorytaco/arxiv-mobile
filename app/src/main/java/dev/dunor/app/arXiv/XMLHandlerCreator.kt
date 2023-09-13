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

class XMLHandlerCreator : DefaultHandler() {
    // Fields
    private var in_a = false
    val creators: MutableList<String> = mutableListOf()

    // Methods
    // Gets be called on the following structure: <tag>characters</tag>
    override fun characters(ch: CharArray, start: Int, length: Int) {
        if (in_a)
            creators.add(String(ch, start, length))
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        // Nothing to do
    }

    // Gets be called on closing tags like: </tag>
    @Throws(SAXException::class)
    override fun endElement(namespaceURI: String, localName: String, qName: String) {
        if (localName == "a") {
            in_a = false
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
        if (localName == "a") {
            in_a = true
        }
    }
}
