package com.bootcamp.balitasehat.model

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bootcamp.balitasehat.R
import com.bootcamp.balitasehat.ResultActivity

class HistoryAdapter(
    private val listHistory: List<HistoryModel>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvDetail: TextView = itemView.findViewById(R.id.tvDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listHistory[position]

        holder.tvNama.text = data.nama
        holder.tvDetail.text = """
        Tanggal Lahir : ${data.tanggalLahir}
        Tanggal Input : ${data.tanggalInput}
        Umur          : ${data.umur} bulan
        Gender        : ${data.gender}
        TB            : ${data.tinggi} cm
        BB            : ${data.berat} kg
    """.trimIndent()

        // ✅ KLIK → RESULT ACTIVITY (AMAN)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ResultActivity::class.java)

            val umur = data.umur.toIntOrNull() ?: 0
            val tinggi = data.tinggi.toDoubleOrNull() ?: 0.0
            val berat = data.berat.toDoubleOrNull() ?: 0.0

            intent.putExtra("from_history", true)
            intent.putExtra("nama", data.nama)
            intent.putExtra("umur", umur)
            intent.putExtra("gender", data.gender)
            intent.putExtra("tinggi", tinggi)
            intent.putExtra("berat", berat)
            intent.putExtra("tanggal_input", data.tanggalInput) // ✅ WAJIB

            context.startActivity(intent)
        }

    }


    override fun getItemCount(): Int = listHistory.size
}
