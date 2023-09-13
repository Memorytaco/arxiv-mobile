package dev.dunor.app.arXiv

import android.app.Dialog
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.ScrollView
import android.widget.TabHost
import android.widget.TabWidget
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.xml.sax.InputSource
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import java.util.regex.Pattern
import javax.xml.parsers.SAXParserFactory

class ArxivMain : AppCompatActivity(), OnItemClickListener {
    object ArxivMenu {
        var items = arrayOf("Astrophysics", "Condensed Matter", "Computer Science",
                "General Relativity", "HEP Experiment", "HEP Lattice",
                "HEP Phenomenology", "HEP Theory", "Mathematics",
                "Mathematical Physics", "Misc Physics", "Nonlinear Sciences",
                "Nuclear Experiment", "Nuclear Theory", "Quantitative Biology",
                "Quantitative Finance", "Quantum Physics", "Statistics")
        var itemsFlag = intArrayOf(1, 2, 3, 0, 0, 0, 0, 0, 4, 0, 5, 6, 0, 0, 7, 8, 0, 9)
        var shortItems = arrayOf("Astrophysics", "Condensed Matter",
                "Computer Science", "General Relativity", "HEP Experiment",
                "HEP Lattice", "HEP Phenomenology", "HEP Theory", "Mathematics",
                "Math. Physics", "Misc Physics", "Nonlinear Sci.", "Nuclear Exp.",
                "Nuclear Theory", "Quant. Biology", "Quant. Finance",
                "Quantum Physics", "Statistics")
        var urls = arrayOf("astro-ph", "cond-mat", "cs", "gr-qc", "hep-ex",
                "hep-lat", "hep-ph", "hep-th", "math", "math-ph", "physics",
                "nlin", "nucl-ex", "nucl-th", "q-bio", "q-fin", "quant-ph", "stat")
        var asItems = arrayOf("Astrophysics All",
                "Cosmology and Extragalactic Astrophysics",
                "Earth & Planetary Astrophysics", "Galaxy Astrophysics",
                "HE Astrophysical Phenomena",
                "Instrumentation and Methods for Astrophysics",
                "Solar and Stellar Astrophysics")
        var asURLs = arrayOf("astro-ph", "astro-ph.CO", "astro-ph.EP",
                "astro-ph.GA", "astro-ph.HE", "astro-ph.IM", "astro-ph.SR")
        var asShortItems = arrayOf("Astrophysics All",
                "Cosm. & Ext-Gal. Astrophysics", "Earth & Planetary Astrophysics",
                "Galaxy Astrophysics", "HE Astrophysical Phenomena",
                "Instrumentation and Methods for Astrophysics",
                "Solar and Stellar Astrophysics")
        var cmItems = arrayOf("Condensed Matter All",
                "Disordered Systems and Neural Networks", "Materials Science",
                "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
                "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
                "Strongly Correlated Electrons", "Superconductivity")
        var cmURLs = arrayOf("cond-mat", "cond-mat.dis-nn", "cond-mat.mtrl-sci",
                "cond-mat.mes-hall", "cond-mat.other", "cond-mat.quant-gas",
                "cond-mat.soft", "cond-mat.stat-mech", "cond-mat.str-el",
                "cond-mat.supr-con")
        var cmShortItems = arrayOf("Cond. Matter All",
                "Disord. Systems & Neural Networks", "Materials Science",
                "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
                "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
                "Strongly Correlated Electrons", "Superconductivity")
        var csItems = arrayOf("Computer Science All", "Architecture",
                "Artificial Intelligence", "Computation and Language",
                "Computational Complexity",
                "Computational Engineering, Finance and Science",
                "Computational Geometry", "CS and Game Theory",
                "Computer Vision and Pattern Recognition", "Computers and Society",
                "Cryptography and Security", "Data Structures and Algorithms",
                "Databases", "Digital Libraries", "Discrete Mathematics",
                "Distributed, Parallel, and Cluster Computing",
                "Formal Languages and Automata Theory", "General Literature",
                "Graphics", "Human-Computer Interaction", "Information Retrieval",
                "Information Theory", "Learning", "Logic in Computer Science",
                "Mathematical Software", "Multiagent Systems", "Multimedia",
                "Networking and Internet Architecture",
                "Neural and Evolutionary Computing", "Numerical Analysis",
                "Operating Systems", "Other Computer Science", "Performance",
                "Programming Languages", "Robotics", "Software Engineering",
                "Sound", "Symbolic Computation")
        var csURLs = arrayOf("cs", "cs.AR", "cs.AI", "cs.CL", "cs.CC", "cs.CE",
                "cs.CG", "cs.GT", "cs.CV", "cs.CY", "cs.CR", "cs.DS", "cs.DB",
                "cs.DL", "cs.DM", "cs.DC", "cs.FL", "cs.GL", "cs.GR", "cs.HC",
                "cs.IR", "cs.IT", "cs.LG", "cs.LO", "cs.MS", "cs.MA", "cs.MM",
                "cs.NI", "cs.NE", "cs.NA", "cs.OS", "cs.OH", "cs.PF", "cs.PL",
                "cs.RO", "cs.SE", "cs.SD", "cs.SC")
        var csShortItems = arrayOf("Computer Science All", "Architecture",
                "Artificial Intelligence", "Computation and Language",
                "Computational Complexity",
                "Comp. Eng., Fin. & Science",
                "Computational Geometry", "CS and Game Theory",
                "Computer Vision and Pattern Recognition", "Computers and Society",
                "Cryptography and Security", "Data Structures and Algorithms",
                "Databases", "Digital Libraries", "Discrete Mathematics",
                "Distributed, Parallel, and Cluster Computing",
                "Formal Languages and Automata Theory", "General Literature",
                "Graphics", "Human-Computer Interaction", "Information Retrieval",
                "Information Theory", "Learning", "Logic in Computer Science",
                "Mathematical Software", "Multiagent Systems", "Multimedia",
                "Networking and Internet Architecture",
                "Neural and Evolutionary Computing", "Numerical Analysis",
                "Operating Systems", "Other Computer Science", "Performance",
                "Programming Languages", "Robotics", "Software Engineering",
                "Sound", "Symbolic Computation")
        var mtItems = arrayOf("Math All", "Algebraic Geometry",
                "Algebraic Topology", "Analysis of PDEs", "Category Theory",
                "Classical Analysis of ODEs", "Combinatorics",
                "Commutative Algebra", "Complex Variables",
                "Differential Geometry", "Dynamical Systems",
                "Functional Analysis", "General Mathematics", "General Topology",
                "Geometric Topology", "Group Theory", "Math History and Overview",
                "Information Theory", "K-Theory and Homology", "Logic",
                "Mathematical Physics", "Metric Geometry", "Number Theory",
                "Numerical Analysis", "Operator Algebras",
                "Optimization and Control", "Probability", "Quantum Algebra",
                "Representation Theory", "Rings and Algebras", "Spectral Theory",
                "Statistics (Math)", "Symplectic Geometry")
        var mtURLs = arrayOf("math", "math.AG", "math.AT", "math.AP", "math.CT",
                "math.CA", "math.CO", "math.AC", "math.CV", "math.DG", "math.DS",
                "math.FA", "math.GM", "math.GN", "math.GT", "math.GR", "math.HO",
                "math.IT", "math.KT", "math.LO", "math.MP", "math.MG", "math.NT",
                "math.NA", "math.OA", "math.OC", "math.PR", "math.QA", "math.RT",
                "math.RA", "math.SP", "math.ST", "math.SG")
        var mtShortItems = arrayOf("Math All", "Algebraic Geometry",
                "Algebraic Topology", "Analysis of PDEs", "Category Theory",
                "Classical Analysis of ODEs", "Combinatorics",
                "Commutative Algebra", "Complex Variables",
                "Differential Geometry", "Dynamical Systems",
                "Functional Analysis", "General Mathematics", "General Topology",
                "Geometric Topology", "Group Theory", "Math History and Overview",
                "Information Theory", "K-Theory and Homology", "Logic",
                "Mathematical Physics", "Metric Geometry", "Number Theory",
                "Numerical Analysis", "Operator Algebras",
                "Optimization and Control", "Probability", "Quantum Algebra",
                "Representation Theory", "Rings and Algebras", "Spectral Theory",
                "Statistics (Math)", "Symplectic Geometry")
        var mpItems = arrayOf("Physics (Misc) All", "Accelerator Physics",
                "Atmospheric and Oceanic Physics", "Atomic Physics",
                "Atomic and Molecular Clusters", "Biological Physics",
                "Chemical Physics", "Classical Physics", "Computational Physics",
                "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
                "General Physics", "Geophysics", "History of Physics",
                "Instrumentation and Detectors", "Medical Physics", "Optics",
                "Physics Education", "Physics and Society", "Plasma Physics",
                "Popular Physics", "Space Physics")
        var mpURLs = arrayOf("physics", "physics.acc-ph", "physics.ao-ph",
                "physics.atom-ph", "physics.atm-clus", "physics.bio-ph",
                "physics.chem-ph", "physics.class-ph", "physics.comp-ph",
                "physics.data-an", "physics.flu-dyn", "physics.gen-ph",
                "physics.geo-ph", "physics.hist-ph", "physics.ins-det",
                "physics.med-ph", "physics.optics", "physics.ed-ph",
                "physics.soc-ph", "physics.plasm-ph", "physics.pop-ph",
                "physics.space-ph")
        var mpShortItems = arrayOf("Physics (Misc) All", "Accelerator Physics",
                "Atmospheric and Oceanic Physics", "Atomic Physics",
                "Atomic and Molecular Clusters", "Biological Physics",
                "Chemical Physics", "Classical Physics", "Computational Physics",
                "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
                "General Physics", "Geophysics", "History of Physics",
                "Instrumentation and Detectors", "Medical Physics", "Optics",
                "Physics Education", "Physics and Society", "Plasma Physics",
                "Popular Physics", "Space Physics")
        var nlItems = arrayOf("Nonlinear Sciences All",
                "Adaptation and Self-Organizing Systems",
                "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
                "Exactly Solvable and Integrable Systems",
                "Pattern Formation and Solitons")
        var nlURLs = arrayOf("nlin", "nlin.AO", "nlin.CG", "nlin.CD", "nlin.SI",
                "nlin.PS")
        var nlShortItems = arrayOf("Nonlinear Sciences",
                "Adaptation and Self-Organizing Systems",
                "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
                "Exactly Solvable and Integrable Systems",
                "Pattern Formation and Solitons")
        var qbItems = arrayOf("Quant. Biology All", "Biomolecules", "Cell Behavior",
                "Genomics", "Molecular Networks", "Neurons and Cognition",
                "Quant. Biology Other", "Populations and Evolutions",
                "Quantitative Methods", "Subcellular Processes",
                "Tissues and Organs")
        var qbURLs = arrayOf("q-bio", "q-bio.BM", "q-bio.CB", "q-bio.GN",
                "q-bio.MN", "q-bio.NC", "q-bio.OT", "q-bio.PE", "q-bio.QM",
                "q-bio.SC", "q-bio.TO")
        var qbShortItems = arrayOf("Quant. Bio. All", "Biomolecules",
                "Cell Behavior", "Genomics", "Molecular Networks",
                "Neurons and Cognition", "QB Other", "Populations and Evolutions",
                "Quantitative Methods", "Subcellular Processes",
                "Tissues and Organs")
        var qfItems = arrayOf("Quant. Finance All", "Computational Finance",
                "General Finance", "Portfolio Management",
                "Pricing and Securities", "Risk Management", "Statistical Finance",
                "Trading and Market Microstructure")
        var qfURLs = arrayOf("q-fin", "q-fin.CP", "q-fin.GN", "q-fin.PM",
                "q-fin.PR", "q-fin.RM", "q-fin.ST", "q-fin.TR")
        var qfShortItems = arrayOf("Quant. Fin. All", "Computational Finance",
                "General Finance", "Portfolio Management",
                "Pricing and Securities", "Risk Management", "Statistical Finance",
                "Trading and Market Microstructure")
        var stItems = arrayOf("Statistics All", "Stats. Applications",
                "Stats. Computation", "Machine Learning", "Stats. Methodology",
                "Stats. Theory")
        var stURLs = arrayOf("stat", "stat.AP", "stat.CO", "stat.ML", "stat.ME",
                "stat.TH")
        var stShortItems = arrayOf("Statistics All", "Stats. Applications",
                "Stats. Computation", "Machine Learning", "Stats. Methodology",
                "Stats. Theory")

    }

    //UI-Views
    private var header: TextView? = null
    private var catList: ListView? = null
    private var favList: ListView? = null
    private var vFlag = 1
    private var mySourcePref = 0
    private var mRemoveAllViews: Method? = null
    private var mAddView: Method? = null
    private val mRemoveAllViewsArgs = arrayOfNulls<Any>(1)
    private val mAddViewArgs = arrayOfNulls<Any>(2)
    private var unreadList: Array<String?> = arrayOf()
    private var favoritesList: Array<String?> = arrayOf()
    private var vFromWidget = false

    // handle top right menu
    private fun applyMenuChoice(item: MenuItem): Boolean {
        when (item.itemId) {
            ABOUT_ID -> {
                object : Dialog(this) {
                    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
                        if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT) dismiss()
                        return true
                    }
                }.apply {
                    setTitle(R.string.about_arxiv_droid)
                    val layoutParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT)
                    addContentView(ScrollView(this@ArxivMain).apply {
                        addView(TextView(this@ArxivMain).apply {
                            setPadding(16, 0, 16, 16)
                            text = getString(R.string.about_text)
                        })
                    }, layoutParam)

                }.show()
                return true
            }

            HISTORY_ID -> {
//                startActivity(Intent(this@ArxivMain, HistoryWindow::class.java))
                return true
            }

            CLEAR_ID -> {
                deleteFiles()
                return true
            }

            PREF_ID -> {
                startActivity(Intent(this@ArxivMain, EditPreferences::class.java))
                return true
            }

            DONATE_ID -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Memorytaco/arxiv-mobile")))
                } catch (ef: Exception) {
                    Toast.makeText(this@ArxivMain, "Not able to jump GITHUB", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return false
    }

    // TODO: refine logic for deleting history and pdf files
    private fun deleteFiles() {
        val dir = File("/sdcard/arXiv")
        val children = dir.list()
        if (children != null) {
            for (i in children.indices) {
                val filename = children[i]
                val f = File("/sdcard/arXiv/$filename")
                if (f.exists()) {
                    f.delete()
                }
            }
        }
        var dir2 = File("/emmc/arXiv")
        var children2 = dir2.list()
        if (children2 != null) {
            for (i in children2.indices) {
                val filename = children2[i]
                val f = File("/emmc/arXiv/$filename")
                if (f.exists()) {
                    f.delete()
                }
            }
        }
        dir2 = File("/media/arXiv")
        children2 = dir2.list()
        if (children2 != null) {
            for (i in children2.indices) {
                val filename = children2[i]
                val f = File("/media/arXiv/$filename")
                if (f.exists()) {
                    f.delete()
                }
            }
        }
        Log.d("Arx", "Opening Database 1")
        val database = ArxivPrivateStorage(this@ArxivMain)
        for (history in database.history) {
            database.deleteHistory(history.historyID)
        }
        database.close()
        Log.d("Arx", "Closed Database 1")
        Toast.makeText(this@ArxivMain, "Deleted PDF history", Toast.LENGTH_SHORT).show()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info: AdapterContextMenuInfo = try {
            item.menuInfo as AdapterContextMenuInfo
        } catch (e: ClassCastException) {
            return false
        }

        Log.d("Arx", "Opening Database 2")
        val database = ArxivPrivateStorage(this@ArxivMain)
        var icount = 0
        if (vFlag == 0) {
            for (feed in database.feeds) {
                if (icount == info.position) {
                    database.deleteFeed(feed.feedId)
                }
                icount++
            }
            val t9: Thread = object : Thread() {
                override fun run() {
                    updateWidget()
                }
            }
            t9.start()
        } else {
            if (mySourcePref == 0) {
                val tempquery = "search_query=cat:" + ArxivMenu.urls[info.position] + "*"
                val tempurl = ("http://export.arxiv.org/api/query?" + tempquery
                        + "&sortBy=submittedDate&sortOrder=ascending")
                database.insertFeed(ArxivMenu.shortItems[info.position], tempquery, tempurl, -1, -1)
                val t9: Thread = object : Thread() {
                    override fun run() {
                        updateWidget()
                    }
                }
                t9.start()
            } else {
                val tempquery = ArxivMenu.urls[info.position]
                database.insertFeed(ArxivMenu.shortItems[info.position] + " (RSS)", ArxivMenu.shortItems[info.position], tempquery, -2, -2)
                Toast.makeText(this@ArxivMain, R.string.added_to_favorites_rss,
                        Toast.LENGTH_SHORT).show()
            }
        }
        database.close()
        Log.d("Arx", "Closed Database 2")
        updateFavList()
        return true
    }

  override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version = Build.VERSION.SDK_INT
      setContentView(R.layout.mainnew)
        header = findViewById<View>(R.id.theader) as TextView
        catList = findViewById<View>(R.id.catlist) as ListView
        favList = findViewById<View>(R.id.favlist) as ListView
        catList!!.onItemClickListener = this
        favList!!.onItemClickListener = this
        val face = Typeface.createFromAsset(assets,"fonts/LiberationSans.ttf")
        header!!.typeface = face
        val tabs = findViewById<View>(R.id.tabhost) as TabHost
        tabs.setup()
        if (version > 7) {
            var vi: View = LayoutInflater.from(this).inflate(R.layout.my_tab_indicator,
                    tabs.tabWidget, false)
            var tempimg = vi.findViewById<View>(R.id.icon) as ImageView
            var temptxt = vi.findViewById<View>(R.id.title) as TextView
            tempimg.setImageResource(R.drawable.cat)
            temptxt.text = "Categories"
            var spec = tabs.newTabSpec("tag1")
            spec.setContent(R.id.catlist)
            spec.setIndicator(vi)
            tabs.addTab(spec)
            vi = LayoutInflater.from(this).inflate(R.layout.my_tab_indicator,
                    tabs.tabWidget, false)
            tempimg = vi.findViewById<View>(R.id.icon) as ImageView
            temptxt = vi.findViewById<View>(R.id.title) as TextView
            tempimg.setImageResource(R.drawable.fav)
            temptxt.text = "Favorites"
            spec = tabs.newTabSpec("tag2")
            spec.setContent(R.id.favlist)
            spec.setIndicator(vi)
            tabs.addTab(spec)
            val tabWidget = tabs.tabWidget
            for (i in 0 until tabWidget.childCount) {
                val tabLayout = tabWidget
                        .getChildAt(i) as RelativeLayout
                tabLayout.setBackgroundDrawable(resources
                        .getDrawable(R.drawable.my_tab_indicator))
            }
            try {
                val mSetStripEnabledSignature = arrayOf<Class<*>?>(Boolean::class.javaPrimitiveType)
                val mSetStripEnabled = TabWidget::class.java.getMethod(
                        "setStripEnabled", *mSetStripEnabledSignature)
                var SEArgs = arrayOfNulls<Any>(1)
                SEArgs[0] = java.lang.Boolean.TRUE
                mSetStripEnabled.invoke(tabWidget, *SEArgs)
                val mSetRightStripDrawableSignature = arrayOf<Class<*>?>(Int::class.javaPrimitiveType)
                val mSetRightStripDrawable = TabWidget::class.java.getMethod(
                        "setRightStripDrawable",
                        *mSetRightStripDrawableSignature)
                SEArgs = arrayOfNulls(1)
                SEArgs[0] = R.drawable.tab_bottom_right_v4
                mSetRightStripDrawable.invoke(tabWidget, *SEArgs)
                val mSetLeftStripDrawable = TabWidget::class.java
                        .getMethod("setLeftStripDrawable",
                                *mSetRightStripDrawableSignature)
                SEArgs = arrayOfNulls(1)
                SEArgs[0] = R.drawable.tab_bottom_left_v4
                mSetLeftStripDrawable.invoke(tabWidget, *SEArgs)
            } catch (ef: Exception) {
                Log.e("arXiv - ", "Strip fail: $ef")
            }
        } else {
            var spec = tabs.newTabSpec("tag1")
            spec.setContent(R.id.catlist)
            spec.setIndicator("Categories", resources.getDrawable(R.drawable.cat))
            tabs.addTab(spec)
            spec = tabs.newTabSpec("tag2")
            spec.setContent(R.id.favlist)
            spec.setIndicator("Favorites", resources.getDrawable(R.drawable.fav))
            tabs.addTab(spec)
        }
        // FIXME: R.layout.simple_list_item_1
        println("Before setting up ArrayAdapter, arXiv.kt:502")
        catList!!.adapter = ArrayAdapter(this, R.layout.simple_list, ArxivMenu.items)
        registerForContextMenu(catList)
        Log.d("Arx", "Opening Database 3")
        val database = ArxivPrivateStorage(this)
        Log.d("Arx", "Closed Database 3")
        val lfavorites: MutableList<String> = ArrayList()
        val lunread: MutableList<String> = ArrayList()
        for (feed in database.feeds) {
            val unreadString =
                when {
                    feed.unread > 99 -> "99+"
                    feed.unread == -2 -> "-"
                    feed.unread <= 0 -> "0"
                    else -> "${feed.unread}"
                }
            lfavorites.add(feed.title)
            lunread.add(unreadString)
        }
        database.close()

        favoritesList = arrayOfNulls(lfavorites.size)
        unreadList = arrayOfNulls(lfavorites.size)
        lfavorites.forEachIndexed { index, s -> favoritesList[index] = s }
        lunread.forEachIndexed { index, s -> unreadList[index] = s }

        //favList.setAdapter(new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, lfavorites));
        println("Before CustomAdapter, arXiv.kt:535")
        favList!!.adapter = FavoritesListAdapter(this, favoritesList)
        registerForContextMenu(favList)
        try {
          if (intent.action === Intent.ACTION_VIEW) {
                searchFromUrlId(intent.data)
            } else {
              val mytype = intent.getStringExtra("keywidget")
              if (mytype != null) {
                vFromWidget = true
                tabs.setCurrentTabByTag("tag2")
              }
            }
        } catch (ef: Exception) {
            Log.e("arxiv", "Failed to change tab $ef")
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        mySourcePref = prefs.getString("sourcelist", "0")!!.toInt()
        println("End onCreate, arXiv.kt:554")
    }

    private fun extractArXivId(data: Uri?): String {
        val pattern = Pattern.compile(ARXIV_PATTERN)
        val matcher = pattern.matcher(data.toString())
        return if (matcher.find()) {
            matcher.group(2)
        } else {
            ""
        }
    }

    private fun searchFromUrlId(data: Uri?) {
        val arXivId = extractArXivId(data)
        if (arXivId.isNotEmpty()) {
            Log.i("arXiv", arXivId)
//            val myIntent = Intent(this, SearchListWindow::class.java)
//            myIntent.putExtra("keyquery", "id_list=$arXivId")
//            myIntent.putExtra("keyname", arXivId)
//            myIntent.putExtra("keyurl", "")
//            startActivity(myIntent)
        } else {
            Log.e("arXiv", "No arXivId found in intent URL")
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo) {
        vFlag = if (view.id == R.id.favlist) {
            menu.add(0, 1000, 0, R.string.remove_favorites)
            0
        } else {
            menu.add(0, 1000, 0, R.string.add_favorites)
            1
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.apply {
            add(Menu.NONE, ABOUT_ID, Menu.NONE, R.string.about_arxiv_droid)
            add(Menu.NONE, HISTORY_ID, Menu.NONE, R.string.view_history)
            add(Menu.NONE, CLEAR_ID, Menu.NONE, R.string.clear_history)
            add(Menu.NONE, PREF_ID, Menu.NONE, R.string.preferences)
            add(Menu.NONE, DONATE_ID, Menu.NONE, R.string.donate)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onItemClick(a: AdapterView<*>, v: View, position: Int, id: Long) {
        println("Item clicked $position")
        fun Intent.emitSearch(keyquery: String? = null, keyname: String?, keyurl: String?) {
            putExtra("keyquery", keyquery)
            putExtra("keyname", keyname)
            putExtra("keyurl", keyurl)
            try {
                this@ArxivMain.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@ArxivMain, "Failed to call relevant intent", Toast.LENGTH_SHORT).show()
                return
            }
        }

        when {
            a.id == R.id.favlist -> {
              Log.d("Arx", "Opening Database 4")
              val database = ArxivPrivateStorage(this)
              val feed = database.feeds[position]
              Log.d("Arx", "Closed Database 4")
              database.close()
            }
            ArxivMenu.itemsFlag[position] == 0 && mySourcePref == 1 -> {
//                Intent(this, RSSListWindow::class.java)
//                        .emitSearch(keyname = ArxivMenu.shortItems[position], keyurl = ArxivMenu.urls[position])
            }
            ArxivMenu.itemsFlag[position] == 0 -> {
                val query = "search_query=cat:${ArxivMenu.urls[position]}*"
//                Intent(this, SearchListWindow::class.java)
//                        .emitSearch(
//                                keyname = ArxivMenu.shortItems[position],
//                                keyquery = query,
//                                keyurl = "http://export.arxiv.org/api/query?${query}&sortBy=submittedDate&sortOrder=ascending"
//                        )
            }
          else -> {
              val myIntent = SubArxivIntent(this, SubarXiv::class.java).apply {
                  name = ArxivMenu.shortItems[position]
              }
              when (ArxivMenu.itemsFlag[position]) {
                  1 -> myIntent.apply {
                      items = ArxivMenu.asItems
                      urls = ArxivMenu.asURLs
                      shortItems = ArxivMenu.asShortItems
                  }

                  2 -> myIntent.apply {
                      items = ArxivMenu.cmItems
                      urls = ArxivMenu.cmURLs
                      shortItems = ArxivMenu.cmShortItems
                  }

                  3 -> myIntent.apply {
                      items = ArxivMenu.csItems
                      urls = ArxivMenu.csURLs
                      shortItems = ArxivMenu.csShortItems
                  }

                  4 -> myIntent.apply {
                      items = ArxivMenu.mtItems
                      urls = ArxivMenu.mtURLs
                      shortItems = ArxivMenu.mtShortItems
                  }

                  5 -> myIntent.apply {
                      items = ArxivMenu.mpItems
                      urls = ArxivMenu.mpURLs
                      shortItems = ArxivMenu.mpShortItems
                  }

                  6 -> myIntent.apply {
                      items = ArxivMenu.nlItems
                      urls = ArxivMenu.nlURLs
                      shortItems = ArxivMenu.nlShortItems
                  }

                  7 -> myIntent.apply {
                      items = ArxivMenu.qbItems
                      urls = ArxivMenu.qbURLs
                      shortItems = ArxivMenu.qbShortItems
                  }

                  8 -> myIntent.apply {
                      myIntent.putExtra("keyitems", ArxivMenu.qfItems)
                      myIntent.putExtra("keyurls", ArxivMenu.qfURLs)
                      myIntent.putExtra("keyshortitems", ArxivMenu.qfShortItems)
                  }

                  9 -> myIntent.apply {
                      items = ArxivMenu.stItems
                      urls = ArxivMenu.stURLs
                      shortItems = ArxivMenu.stShortItems
                  }
              }
              startActivity(myIntent)
          }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return applyMenuChoice(item) || super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        mySourcePref = prefs.getString("sourcelist", "0")!!.toInt()
        Log.d("Arx", "Opening Database 5")
        val database = ArxivPrivateStorage(this)
        if (!vFromWidget) {
            //Should check for new articles?
            val t10: Thread = object : Thread() {
                override fun run() {
                    updateWidget()
                }
            }
            t10.start()
        }
        val lfavorites: MutableList<String?> = ArrayList()
        val lunread: MutableList<String> = ArrayList()
        for (feed in database.feeds) {
            var unreadString = ""
            unreadString = if (feed!!.unread > 99) {
                "99+"
            } else if (feed.unread == -2) {
                "-"
            } else if (feed.unread <= 0) {
                "0"
            } else if (feed.unread < 10) {
                "" + feed.unread
            } else {
                "" + feed.unread
            }
            lfavorites.add(feed.title)
            lunread.add(unreadString)
        }
        Log.d("Arx", "Closed Database 5")
        database.close()
        favoritesList = arrayOfNulls(lfavorites.size)
        unreadList = arrayOfNulls(lfavorites.size)
        lfavorites.forEachIndexed { index, s -> favoritesList[index] = s}
        lunread.forEachIndexed { index, s -> unreadList[index] = s }

        favList!!.adapter = FavoritesListAdapter(this, favoritesList)
        registerForContextMenu(favList)
    }

    private fun populateMenu(menu: Menu) {


    }

    fun searchPressed(buttoncover: View?) {
        val myIntent = Intent(this, SearchWindow::class.java)
        startActivity(myIntent)
    }

    fun updateFavList() {
        Log.d("Arx", "Opening Database 6")
        val database = ArxivPrivateStorage(this)
        val lfavorites: MutableList<String?> = ArrayList()
        val lunread: MutableList<String> = ArrayList()
        for (feed in database.feeds) {
            var unreadString = ""
            unreadString = if (feed.unread > 99) {
                "99+"
            } else if (feed.unread == -2) {
                "-"
            } else if (feed.unread <= 0) {
                "0"
            } else if (feed.unread < 10) {
                "" + feed.unread
            } else {
                "" + feed.unread
            }
            lfavorites.add(feed.title)
            lunread.add(unreadString)
        }
        Log.d("Arx", "Closed Database 6")
        database.close()
        favoritesList = arrayOfNulls(lfavorites.size)
        unreadList = arrayOfNulls(lfavorites.size)
        lfavorites.forEachIndexed { index, s ->  favoritesList[index] = s}
        lunread.forEachIndexed { index, s ->  unreadList[index] = s}

        //favList.setAdapter(new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, lfavorites));
        favList!!.adapter = FavoritesListAdapter(this, favoritesList)
        registerForContextMenu(favList)
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
        Log.d("Arx", "Opening Database 7")
        val database = ArxivPrivateStorage(this)
        val favorites = database.feeds
        database.close()
        Log.d("Arx", "Closed Database 7")
        var favText = ""
        if (favorites.isNotEmpty()) {
            var vUnreadChanged = false
            try {
                mRemoveAllViews = RemoteViews::class.java.getMethod("removeAllViews",
                        *mRemoveAllViewsSignature)
                mRemoveAllViewsArgs[0] = Integer.valueOf(R.id.mainlayout)
                mRemoveAllViews!!.invoke(views, *mRemoveAllViewsArgs)

                //views.removeAllViews(R.id.mainlayout);
            } catch (ef: Exception) {
            }
            for (feed in favorites) {
                if (feed.url.contains("query")) {
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
                    favText = feed.title
                    if (feed.count > -1) {
                        val newArticles = numberOfTotalResults - feed.count
                        if (newArticles >= 0) {
                            tempViews.setTextViewText(R.id.number, "" + newArticles)
                        } else {
                            tempViews.setTextViewText(R.id.number, "0")
                        }
                        if (newArticles != feed.unread) {
                            vUnreadChanged = true
                            val dDB = ArxivPrivateStorage(this)
                            dDB.updateFeed(feed.feedId, feed.title, feed.shortTitle, feed.url, feed.count, newArticles)
                            dDB.close()
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
                        views.setTextViewText(R.id.subheading, "Widget only supported on Android 2.1+")
                    }
                }
                val thisWidget = ComponentName(this, ArxivAppWidgetProvider::class.java)
                val manager = AppWidgetManager.getInstance(this)
                manager.updateAppWidget(thisWidget, views)
            }
            if (vUnreadChanged) {
                handlerSetList.sendEmptyMessage(0)
            }
        }
    }

  class FavoritesListAdapter(val context: ArxivMain, private val items: Array<String?>) : ArrayAdapter<String>(context, R.layout.favoritesrow, items) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var row = convertView
            val holder: ViewHolder
            if (row == null) {
                val inflater = LayoutInflater.from(context)
                row = inflater.inflate(R.layout.favoritesrow, parent, false)
                holder = ViewHolder()
                holder.text1 = row!!.findViewById<View>(R.id.text1) as TextView?
                holder.text2 = row.findViewById<View>(R.id.text2) as TextView?
              row.tag = holder
            } else {
                holder = row.tag as ViewHolder
            }
            holder.text1!!.text = context.unreadList[position]
            holder.text2!!.text = context.favoritesList[position]
            return row
        }

        inner class ViewHolder {
            var text1: TextView? = null
            var text2: TextView? = null
        }
    }

    private val handlerSetList: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            updateFavList()
        }
    }

    companion object {
        const val ARXIV_PATTERN = "^https?://arxiv.org/(abs|pdf)/([0-9]+\\.?[0-9]+).*"
        const val ABOUT_ID = Menu.FIRST + 1
        const val HISTORY_ID = Menu.FIRST + 2
        const val CLEAR_ID = Menu.FIRST + 3
        const val PREF_ID = Menu.FIRST + 4
        const val DONATE_ID = Menu.FIRST + 5
        private val mRemoveAllViewsSignature = arrayOf<Class<*>?>(
                Int::class.javaPrimitiveType)
        private val mAddViewSignature = arrayOf(
                Int::class.javaPrimitiveType, RemoteViews::class.java)
    }

}
