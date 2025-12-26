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
        val context = holder.itemView.context

        holder.tvNama.text = data.nama

        // ✅ Gunakan classification baru jika tersedia
        val statusText = if (!data.classificationHeight.isNullOrEmpty() &&
                            !data.classificationWeight.isNullOrEmpty()) {
            "TB: ${data.classificationHeight} | BB: ${data.classificationWeight}"
        } else {
            // Fallback ke status lama
            data.stuntingStatus ?: "-"
        }

        holder.tvDetail.text = """
        Tanggal Input : ${data.tanggalInput}
        Umur          : ${data.umur} bulan
        TB            : ${data.tinggi} cm
        BB            : ${data.berat} kg
        Status        : $statusText
    """.trimIndent()

        // ✅ KLIK → RESULT ACTIVITY
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ResultActivity::class.java)

            val umur = data.umur.toIntOrNull() ?: 0
            val tinggi = data.tinggi.toDoubleOrNull() ?: 0.0
            val berat = data.berat.toDoubleOrNull() ?: 0.0

            intent.putExtra("from_history", true)
            intent.putExtra("child_id", data.childId)
            intent.putExtra("nama", data.nama)
            intent.putExtra("age_months", umur)
            intent.putExtra("height_cm", tinggi)
            intent.putExtra("weight_kg", berat)
            intent.putExtra("measurement_date", data.tanggalInput)
            intent.putExtra("height_zscore", data.zscoreHeight)
            intent.putExtra("weight_zscore", data.zscoreWeight)
            intent.putExtra("classification_height", data.classificationHeight)
            intent.putExtra("classification_weight", data.classificationWeight)
            intent.putExtra("risk_level", data.riskLevel)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listHistory.size
}

