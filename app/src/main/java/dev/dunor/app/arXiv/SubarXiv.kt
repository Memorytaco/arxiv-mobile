package dev.dunor.app.arXiv

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.xml.sax.InputSource
import java.lang.reflect.Method
import java.net.URL
import javax.xml.parsers.SAXParserFactory

class SubArxivIntent<T>: Intent {

    constructor(context: Context, cls: Class<T>) : super(context, cls)
    constructor(intent: Intent): super(intent)

    var name: String
        get () = getStringExtra(KEY_NAME)!!
        set(value) {
            putExtra(KEY_NAME, value)
        }

    var urls: Array<String>
        get () = getStringArrayExtra(KEY_URLS)!!
        set(value) {
            putExtra(KEY_URLS, value)
        }

    var items: Array<String>
        get() = getStringArrayExtra(KEY_ITEMS)!!
        set(value) {
            putExtra(KEY_ITEMS, value)
        }

    var shortItems: Array<String>
        get() = getStringArrayExtra(KEY_SHORT_ITEMS)!!
        set(value) {
            putExtra(KEY_SHORT_ITEMS, value)
        }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_ITEMS = "items"
        const val KEY_URLS = "urls"
        const val KEY_SHORT_ITEMS = "short_items"
    }

}

class SubarXiv : AppCompatActivity(), OnItemClickListener {
    private var thisActivity: Context? = null

    //UI-Views
    private var headerTextView: TextView? = null
    var list: ListView? = null
    private lateinit var urls: Array<String>
    private lateinit var shortItems: Array<String>
    private var mRemoveAllViews: Method? = null
    private var mAddView: Method? = null
    private val mRemoveAllViewsArgs = arrayOfNulls<Any>(1)
    private val mAddViewArgs = arrayOfNulls<Any>(2)
    private var mySourcePref = 0

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info: AdapterContextMenuInfo = try {
            item.menuInfo as AdapterContextMenuInfo
        } catch (e: ClassCastException) {
            return false
        }
        val database = ArxivPrivateStorage(this)
        if (mySourcePref == 0) {
            var tempquery = "search_query=cat:" + urls!![info.position]
            if (info.position == 0) {
                tempquery = "$tempquery*"
            }
            val tempurl = ("http://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending")
            database.insertFeed(shortItems!![info.position],
                    tempquery, tempurl, -1, -1)
            val t9: Thread = object : Thread() {
                override fun run() {
                    updateWidget()
                }
            }
            t9.start()
        } else {
            val tempquery = urls!![info.position]
            database.insertFeed(shortItems!![info.position] + " (RSS)", shortItems!![info.position], tempquery, -2, -2)
            Toast.makeText(this, R.string.added_to_favorites_rss,
                    Toast.LENGTH_SHORT).show()
        }
        database.close()
        return true
    }

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.submain)
        val myIntent = SubArxivIntent<SubarXiv>(intent = intent)
        urls = myIntent.urls
        shortItems = myIntent.shortItems
        headerTextView = findViewById<View>(R.id.theadersm) as TextView
        val face = Typeface.createFromAsset(assets,"fonts/LiberationSans.ttf")
        headerTextView!!.typeface = face
        list = findViewById<View>(R.id.listsm) as ListView
        thisActivity = this
        headerTextView!!.text = myIntent.name
        list!!.adapter = ArrayAdapter(this, R.layout.simple_list, myIntent.items)
        list!!.onItemClickListener = this
        registerForContextMenu(list)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        mySourcePref = prefs.getString("sourcelist", "0")!!.toInt()
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View,
                                     menuInfo: ContextMenuInfo) {
        menu.add(0, 1000, 0, R.string.add_favorites)
    }

    override fun onItemClick(a: AdapterView<*>?, v: View, position: Int, id: Long) {
        if (mySourcePref == 0) {
//            val myIntent = Intent(this, SearchListWindow::class.java)
//            myIntent.putExtra("keyname", shortItems!![position])
//            var tempquery = "search_query=cat:" + urls[position]
//            if (position == 0) {
//                tempquery = "$tempquery*"
//            }
//            myIntent.putExtra("keyquery", tempquery)
//            val tempurl = ("http://export.arxiv.org/api/query?" + tempquery
//                    + "&sortBy=submittedDate&sortOrder=ascending")
//            myIntent.putExtra("keyurl", tempurl)
//            startActivity(myIntent)
        } else {
//            val myIntent = Intent(this, RSSListWindow::class.java)
//            myIntent.putExtra("keyname", shortItems!![position])
//            myIntent.putExtra("keyurl", urls!![position])
//            startActivity(myIntent)
        }
    }

    fun updateWidget() {
        // Get the layout for the App Widget and attach an on-click listener to the button
        val context = applicationContext
        val views = RemoteViews(context.packageName, R.layout.arxiv_appwidget)
        // Create an Intent to launch ExampleActivity
        val intent = Intent(context, ArxivMain::class.java)
        val typestring = "widget"
        intent.putExtra("keywidget", typestring)
        intent.setData(Uri.parse("foobar://" + SystemClock.elapsedRealtime()))
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.mainlayout, pendingIntent)
        val database = ArxivPrivateStorage(thisActivity!!)
        val favorites = database.feeds
        database.close()
        var favText = ""
        if (favorites!!.size > 0) {
            try {
                mRemoveAllViews = RemoteViews::class.java.getMethod("removeAllViews",
                        *mRemoveAllViewsSignature)
                mRemoveAllViewsArgs[0] = Integer.valueOf(R.id.mainlayout)
                mRemoveAllViews!!.invoke(views, *mRemoveAllViewsArgs)

                //views.removeAllViews(R.id.mainlayout);
            } catch (ef: Exception) {
            }
            for (feed in favorites) {
                if (feed.url!!.contains("query")) {
                    val urlAddressTemp = ("http://export.arxiv.org/api/query?" + feed.shortTitle
                            + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=1")
                    var numberOfTotalResults = 0
                    try {
                        val url = URL(urlAddressTemp)
                        val spf = SAXParserFactory.newInstance()
                        val sp = spf.newSAXParser()
                        val xr = sp.xmlReader
                        val myXMLHandler = XMLHandlerSearch()
                        xr.contentHandler = myXMLHandler
                        xr.parse(InputSource(url.openStream()))
                        numberOfTotalResults = myXMLHandler.numTotalItems
                    } catch (ef: Exception) {
                    }
                    val tempViews = RemoteViews(context.packageName, R.layout.arxiv_appwidget_item)
                    favText = feed.title!!
                    if (feed.count > -1) {
                        val newArticles = numberOfTotalResults - feed.count
                        tempViews.setTextViewText(R.id.number, "" + newArticles)
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
                    } catch (ef: Exception) {
                        views.setTextViewText(R.id.subheading, "Widget only supported on Android 2.1+")
                    }
                }
                val thisWidget = ComponentName(thisActivity!!, ArxivAppWidgetProvider::class.java)
                val manager = AppWidgetManager.getInstance(thisActivity)
                manager.updateAppWidget(thisWidget, views)
            }
        }
    }

    companion object {
        private val mRemoveAllViewsSignature = arrayOf<Class<*>?>(
                Int::class.javaPrimitiveType)
        private val mAddViewSignature = arrayOf(
                Int::class.javaPrimitiveType, RemoteViews::class.java)
    }
}
