package com.example.simplesql

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*


class MainActivity : AppCompatActivity() {

    val TAG = "SQLiteActivity.class"
    val STATE_ERROR = "error"
    val STATE_WARN = "warn"
    val STATE_INFO = "info"
    val STATE_LOG = "log"

    protected lateinit var sqlQueryTable : TableLayout
    protected lateinit var db : SQLiteDatabase
    protected lateinit var sqlExecEdit : AutoCompleteTextView
    protected lateinit var sqlStateText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sqlQueryTable = findViewById(R.id.sqlQueryTable)
        sqlExecEdit = findViewById(R.id.sqlExecEdit)
        sqlStateText = findViewById(R.id.sqlStateText)

        db = openOrCreateDatabase("simple_sql.db", MODE_PRIVATE, null)

        val sqlAdapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.sql_commands,
            android.R.layout.simple_dropdown_item_1line
        )
        sqlExecEdit.setAdapter(sqlAdapter)
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }

    protected fun execSQL(cmd : String){
        try {
            db.execSQL(cmd)
            setState("Command '$cmd' execute success.", STATE_INFO)
        }catch (e : SQLiteException){
            setState(e.message, STATE_ERROR)
        }
    }

    protected fun rawQuery(cmd : String): Array<Array<Any?>?> {
        val cursor : Cursor = db.rawQuery(cmd, null)
        val nrow = cursor.count
        val result = arrayOfNulls<Array<Any?>>(nrow + 1)
        result[0] = cursor.columnNames as Array<Any?>
        for (i in 0 until nrow){
            cursor.moveToNext()
            val ncol = cursor.columnCount
            val rowData = arrayOfNulls<Any?>(ncol)
            for (j in 0 until ncol){
                val type = cursor.getType(j)
                var data : Any? = null
                when(type){
                    Cursor.FIELD_TYPE_FLOAT -> data = cursor.getFloat(j)
                    Cursor.FIELD_TYPE_BLOB -> data = cursor.getBlob(j)
                    Cursor.FIELD_TYPE_INTEGER -> data = cursor.getInt(j)
                    Cursor.FIELD_TYPE_NULL -> data = null
                    Cursor.FIELD_TYPE_STRING -> data = cursor.getString(j)
                }
                rowData[j] = data
            }
            result[i + 1] = rowData
        }
        return result
    }

    protected fun createRow(rowData : Array<Any?>?): TableRow? {
        if(rowData == null) return null
        val tableRow : TableRow = TableRow(this)
        for(i in rowData.indices){
            val textView : TextView = TextView(this)
            textView.setPadding(10, 0, 10, 0)
            textView.text = "${rowData?.get(i).toString()}"
            textView.setTextIsSelectable(true)
            tableRow.addView(textView)
        }
        return tableRow
    }

    protected fun buildTable(tableData : Array<Array<Any?>?>){
        sqlQueryTable.removeAllViews()

        for(i in tableData.indices){
            val tableRow = createRow(tableData[i])
            sqlQueryTable.addView(tableRow)
        }
    }

    protected fun setState(state: String?, type : String){
        sqlStateText.text = state ?: "empty state"
        when(type){
            STATE_ERROR -> sqlStateText.setTextColor(Color.RED)
            STATE_WARN -> sqlStateText.setTextColor(Color.YELLOW)
            STATE_INFO -> sqlStateText.setTextColor(Color.BLUE)
            STATE_LOG -> sqlStateText.setTextColor(Color.BLACK)
            else -> sqlStateText.setTextColor(Color.BLACK)
        }
    }

    fun onExecBnClick(view : View){
        execSQL(sqlExecEdit.text.toString())
    }

    fun onQueryBnClick(view : View){
        try {
            val cmd = sqlExecEdit.text.toString()
            val tableData = rawQuery(cmd)
            buildTable(tableData)
            setState("Command '$cmd' execute success.", STATE_INFO)
        }catch (e : SQLiteException){
            setState(e.message, STATE_ERROR)
        }
    }
}