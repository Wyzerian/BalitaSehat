package com.bootcamp.balitasehat

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bootcamp.balitasehat.model.HistoryAdapter
import com.bootcamp.balitasehat.model.HistoryModel
import org.json.JSONArray

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        rvHistory.layoutManager = LinearLayoutManager(this)

        val listHistory = loadHistory()

        if (listHistory.isEmpty()) {
            tvEmpty.visibility = TextView.VISIBLE
            rvHistory.visibility = RecyclerView.GONE
        } else {
            tvEmpty.visibility = TextView.GONE
            rvHistory.visibility = RecyclerView.VISIBLE
            rvHistory.adapter = HistoryAdapter(listHistory)
        }
    }

    private fun loadHistory(): List<HistoryModel> {
        val sharedPref = getSharedPreferences("history_data", MODE_PRIVATE)
        val jsonString = sharedPref.getString("history_list", "[]")

        val jsonArray = org.json.JSONArray(jsonString)
        val list = mutableListOf<HistoryModel>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            list.add(
                HistoryModel(
                    nama = obj.getString("nama"),
                    umur = obj.getString("umur"),
                    gender = obj.getString("gender"),
                    tinggi = obj.getString("tinggi"),
                    berat = obj.getString("berat"),
                    tanggalLahir = obj.getString("tanggal_lahir"),
                    tanggalInput = obj.getString("tanggal_input")
                )
            )
        }
        return list
    }

}
