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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import org.xml.sax.InputSource
import java.lang.reflect.Method
import java.net.URL
import javax.xml.parsers.SAXParserFactory

class ArxivAppWidgetProvider : AppWidgetProvider() {
    private var mRemoveAllViews: Method? = null
    private var mAddView: Method? = null
    private val mRemoveAllViewsArgs = arrayOfNulls<Any>(1)
    private val mAddViewArgs = arrayOfNulls<Any>(2)
    private var iCounter: Int? = null
    private var views: RemoteViews? = null
    private var thisContext: Context? = null
    private var favText: String? = null
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val N = appWidgetIds.size
        thisContext = context

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (i in 0 until N) {
            val appWidgetId = appWidgetIds[i]

            // Create an Intent to launch ExampleActivity
            val intent = Intent(context, ArxivMain::class.java)
            val typestring = "widget"
            intent.putExtra("keywidget", typestring)
            intent.setData(Uri.parse("foobar://" + SystemClock.elapsedRealtime()))
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // Get the layout for the App Widget and attach an on-click listener to the button
            views = RemoteViews(context.packageName, R.layout.arxiv_appwidget)
            views!!.setOnClickPendingIntent(R.id.mainlayout, pendingIntent)
            try {
                mRemoveAllViews = RemoteViews::class.java.getMethod("removeAllViews",
                        *mRemoveAllViewsSignature)
                mRemoveAllViewsArgs[0] = Integer.valueOf(R.id.mainlayout)
                mRemoveAllViews!!.invoke(views, *mRemoveAllViewsArgs)
                //views.removeAllViews(R.id.mainlayout);
            } catch (ef: Exception) {
            }
            val database = ArxivPrivateStorage(context)
            val favorites = database.feeds
            database.close()
            favText = ""
            Log.d("arXiv", "Updating widget - size " + favorites.size)
            iCounter = 0
            if (favorites.size > 0) {
                for (feed in favorites) {
                    if (feed!!.url!!.contains("query")) {
                        iCounter = iCounter!! + 1
                    }
                }
            }
            val t9: Thread = object : Thread() {
                override fun run() {
                    if (iCounter!! > 0) {
                        for (feed in favorites!!) {
                            if (feed!!.url!!.contains("query")) {
                                val urlAddress = ("http://export.arxiv.org/api/query?" + feed.shortTitle
                                        + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=1")
                                var numberOfTotalResults = 0
                                try {
                                    val url = URL(urlAddress)
                                    val spf = SAXParserFactory.newInstance()
                                    val sp = spf.newSAXParser()
                                    val xr = sp.xmlReader
                                    val myXMLHandler = XMLHandlerSearch()
                                    xr.contentHandler = myXMLHandler
                                    xr.parse(InputSource(url.openStream()))
                                    numberOfTotalResults = myXMLHandler.numTotalItems
                                } catch (ef: Exception) {
                                    Log.d("arXiv", "Caught Exception $ef")
                                }
                                val tempViews = RemoteViews(thisContext!!.packageName, R.layout.arxiv_appwidget_item)
                                favText = feed.title
                                Log.d("arXiv", "Updating widget " + feed.shortTitle + " " + feed.count + " " + numberOfTotalResults)
                                if (feed.count > -1) {
                                    val newArticles = numberOfTotalResults - feed.count
                                    if (newArticles >= 0) {
                                        tempViews.setTextViewText(R.id.number, "" + newArticles)
                                    } else {
                                        tempViews.setTextViewText(R.id.number, "0")
                                    }
                                    if (newArticles != feed.unread) {
                                        val database = ArxivPrivateStorage(thisContext!!)
                                        database.updateFeed(feed.feedId, feed.title, feed.shortTitle, feed.url, feed.count, newArticles)
                                        database.close()
                                    }
                                } else {
                                    tempViews.setTextViewText(R.id.number, "0")
                                }
                                tempViews.setTextViewText(R.id.favtext, favText)
                                try {
                                    mAddView = RemoteViews::class.java.getMethod("addView",
                                            *mAddViewSignature)
                                    mAddViewArgs[0] = Integer.valueOf(R.id.mainlayout)
                                    mAddViewArgs[1] = tempViews
                                    mAddView!!.invoke(views, *mAddViewArgs)
                                    //views.addView(R.id.mainlayout, tempViews);
                                } catch (ef: Exception) {
                                    views!!.setTextViewText(R.id.subheading, "Widget only supported on Android 2.1+")
                                }
                            }
                        }
                    } else {
                        val tempViews = RemoteViews(thisContext!!.packageName, R.layout.arxiv_appwidget_item)
                        favText = "No favorite categories or searches set, or incompatible source preference set in all favorites."
                        tempViews.setTextViewText(R.id.number, "-")
                        tempViews.setTextViewText(R.id.favtext, favText)
                        try {
                            mAddView = RemoteViews::class.java.getMethod("addView",
                                    *mAddViewSignature)
                            mAddViewArgs[0] = Integer.valueOf(R.id.mainlayout)
                            mAddViewArgs[1] = tempViews
                            mAddView!!.invoke(views, *mAddViewArgs)
                            //views.addView(R.id.mainlayout, tempViews);
                        } catch (ef: Exception) {
                            views!!.setTextViewText(R.id.subheading, "Widget only supported on Android 2.1+")
                        }
                    }

                    // Tell the AppWidgetManager to perform an update on the current App Widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
            t9.start()
        }
    }

    companion object {
        private val mRemoveAllViewsSignature = arrayOf<Class<*>?>(
                Int::class.javaPrimitiveType)
        private val mAddViewSignature = arrayOf(
                Int::class.javaPrimitiveType, RemoteViews::class.java)
    }
}
