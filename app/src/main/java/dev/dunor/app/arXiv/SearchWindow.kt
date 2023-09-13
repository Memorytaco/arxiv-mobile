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

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class SearchWindow : Activity(), OnItemSelectedListener, TextWatcher {
    //UI-Views
    private var dateBtn: Button? = null
    private var header: TextView? = null
    private var field1: EditText? = null
    private var field2: EditText? = null
    private var field3: EditText? = null
    private var finalDate: String? = null
    private var query1 = ""
    private var query2 = ""
    private var query3 = ""
    private var textEntryValue1 = ""
    private var textEntryValue2: String? = ""
    private var textEntryValue3: String? = ""
    private val items = arrayOf("Author", "Title", "Abstract", "arXivID")
    private var iSelected1 = 0
    private var iSelected2 = 0
    private var iSelected3 = 0
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private val mDateSetListener = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        dateBtn!!.text = "$mYear-$mMonth-$mDay"
        finalDate = if (mMonth > 9) {
            "" + mYear + (mMonth + 1) + mDay + "2399"
        } else {
            "" + mYear + "0" + (mMonth + 1) + mDay + "2399"
        }
    }

    override fun afterTextChanged(s: Editable) {
        // needed for interface, but not used
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                   after: Int) {
        // needed for interface, but not used
    }

    /** Called when the activity is first created.  */

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search)
        header = findViewById<View>(R.id.theaderse) as TextView
        val face = Typeface.createFromAsset(assets,
                "fonts/LiberationSans.ttf")
        header!!.setTypeface(face)
        header!!.text = "Search"
        dateBtn = findViewById<View>(R.id.datebtn) as Button
        val spin1 = findViewById<View>(R.id.spinner1) as Spinner
        spin1.onItemSelectedListener = this
        val spin2 = findViewById<View>(R.id.spinner2) as Spinner
        spin2.onItemSelectedListener = this
        val spin3 = findViewById<View>(R.id.spinner3) as Spinner
        spin3.onItemSelectedListener = this
        // FIXME: simple_spinner_item
        val aa = ArrayAdapter(this, R.layout.simple_list, items)
        // FIXME: simple_spinner_dropdown_item
        aa.setDropDownViewResource(R.layout.item10)
        spin1.adapter = aa
        spin2.adapter = aa
        spin3.adapter = aa
        field1 = findViewById<View>(R.id.field1) as EditText
        field1!!.addTextChangedListener(this)
        field2 = findViewById<View>(R.id.field2) as EditText
        field2!!.addTextChangedListener(this)
        field3 = findViewById<View>(R.id.field3) as EditText
        field3!!.addTextChangedListener(this)
        val formatter = SimpleDateFormat("yyyyMMdd")
        val currentTime_1 = Date()
        var finalDate = formatter.format(currentTime_1)
        finalDate = finalDate + "2359"
        Log.d("arXiv - ", finalDate)
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]
        finalDate = if (mMonth > 9) {
            "" + mYear + (mMonth + 1) + mDay + "2399"
        } else {
            "" + mYear + "0" + (mMonth + 1) + mDay + "2399"
        }
    }

    private var isSubmit = false
    override fun onPause() {
        super.onPause()
        if (dataLossSave != null) {
            dataLossSave!!.clear()
            dataLossSave = null
        }
        dataLossSave = Bundle()
        if (isSubmit) {
            dataLossSave!!.clear()
            dataLossSave = null
        } else {
            dataLossSave!!.putString("editField1", field1!!.text.toString())
            dataLossSave!!.putString("editField2", field2!!.text.toString())
            dataLossSave!!.putString("editField3", field3!!.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        if (dataLossSave != null) {
            field1!!.setText(dataLossSave!!.getString("editField1", ""))
            field2!!.setText(dataLossSave!!.getString("editField2", ""))
            field3!!.setText(dataLossSave!!.getString("editField3", ""))
        }
    }

    override fun onCreateDialog(id: Int): Dialog? {
        when (id) {
            DATE_DIALOG_ID -> return DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                    mDay)
        }
        return null
    }

    override fun onItemSelected(parent: AdapterView<*>, v: View, position: Int,
                                id: Long) {
        when (parent.id) {
            R.id.spinner1 -> {
                iSelected1 = position
            }
            R.id.spinner2 -> {
                iSelected2 = position
            }
            R.id.spinner3 -> {
                iSelected3 = position
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        var tempt = ""
        tempt = field1!!.text.toString()
        if (textEntryValue1 !== tempt) {
            textEntryValue1 = tempt
        }
        tempt = field2!!.text.toString()
        if (textEntryValue2 !== tempt) {
            textEntryValue2 = tempt
        }
        tempt = field3!!.text.toString()
        if (textEntryValue3 !== tempt) {
            textEntryValue3 = tempt
        }
    }

    fun pressedDateButton(button: View?) {
        showDialog(DATE_DIALOG_ID)
    }

    fun pressedSearchButton(button: View?) {
        isSubmit = true
        var query = ""
        var idlist = ""
        var tittext = "Search: $textEntryValue1"
        if (iSelected1 == 0) {
            query1 = ("au:%22" + textEntryValue1.replace(" ", "+").replace("-", "_")
                    + "%22")
            query = query1
        } else if (iSelected1 == 1) {
            query1 = "ti:%22" + textEntryValue1.replace(" ", "+") + "%22"
            query = query1
        } else if (iSelected1 == 2) {
            query1 = "abs:%22" + textEntryValue1.replace(" ", "+") + "%22"
            query = query1
        } else if (iSelected1 == 3) {
            idlist = idlist + textEntryValue1.replace(" ", ",")
        }
        if (!(textEntryValue2 == null || textEntryValue2 == "")) {
            tittext = "$tittext $textEntryValue2"
            if (iSelected2 == 0) {
                query2 = ("au:%22" + textEntryValue2!!.replace(" ", "+").replace("-", "_")
                        + "%22")
                query = if (!(query == null || query == "")) {
                    "$query+AND+$query2"
                } else {
                    query2
                }
            } else if (iSelected2 == 1) {
                query2 = "ti:%22" + textEntryValue2!!.replace(" ", "+") + "%22"
                query = if (!(query == null || query == "")) {
                    "$query+AND+$query2"
                } else {
                    query2
                }
            } else if (iSelected2 == 2) {
                query2 = "abs:%22" + textEntryValue2!!.replace(" ", "+") + "%22"
                query = if (!(query == null || query == "")) {
                    "$query+AND+$query2"
                } else {
                    query2
                }
            } else if (iSelected2 == 3) {
                idlist = idlist + textEntryValue2!!.replace(" ", ",")
            }
        }
        if (!(textEntryValue3 == null || textEntryValue3 == "")) {
            tittext = "$tittext $textEntryValue3"
            if (iSelected3 == 0) {
                query3 = ("au:%22" + textEntryValue3!!.replace(" ", "+").replace("-", "_")
                        + "%22")
                query = if (!(query == null || query == "")) {
                    "$query+AND+$query3"
                } else {
                    query3
                }
            } else if (iSelected3 == 1) {
                query3 = "ti:%22" + textEntryValue3!!.replace(" ", "+") + "%22"
                query = if (!(query == null || query == "")) {
                    "$query+AND+$query3"
                } else {
                    query3
                }
            } else if (iSelected3 == 2) {
                query3 = "abs:%22" + textEntryValue3!!.replace(" ", "+") + "%22"
                query = if (!(query == null || query == "")) {
                    "$query+AND+$query3"
                } else {
                    query3
                }
            } else if (iSelected3 == 3) {
                idlist = idlist + textEntryValue3!!.replace(" ", ",")
            }
        }
        var totalsearch = ""
        totalsearch = if (query === "" || query == null) {
            ("search_query=lastUpdatedDate:[199008010001+TO+"
                    + finalDate + "]&")
        } else {
            ("search_query=lastUpdatedDate:[199008010001+TO+"
                    + finalDate + "]+AND+" + query + "&")
        }
        totalsearch = totalsearch + "id_list=" + idlist
//        val myIntent = Intent(this, SearchListWindow::class.java)
        if (tittext.length > 30) {
            tittext = tittext.substring(0, 30)
        }
//        myIntent.putExtra("keyname", tittext)
        val urlad = ("http://export.arxiv.org/api/query?"
                + totalsearch
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20")
        Log.d("arXiv - ", urlad)
//        myIntent.putExtra("keyurl", urlad)
//        myIntent.putExtra("keyquery", totalsearch)
//        startActivity(myIntent)
    }

    companion object {
        const val DATE_DIALOG_ID = 0
        private var dataLossSave: Bundle? = null
    }
}
