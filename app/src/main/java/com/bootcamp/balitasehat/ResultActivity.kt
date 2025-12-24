package com.bootcamp.balitasehat

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.model.LmsData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import kotlin.math.pow
import android.graphics.Color

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // ===== TEXTVIEW INFO =====
        val tvNama = findViewById<TextView>(R.id.tvNamaAnak)
        val tvUmur = findViewById<TextView>(R.id.tvUmur)
        val tvGender = findViewById<TextView>(R.id.tvGender)
        val tvTinggi = findViewById<TextView>(R.id.tvTinggi)
        val tvBerat = findViewById<TextView>(R.id.tvBerat)
        val tvTanggalInput = findViewById<TextView>(R.id.tvTanggalInput)

        // ===== CHART =====
        val chartTb = findViewById<LineChart>(R.id.chartTb)
        val chartBb = findViewById<LineChart>(R.id.chartBb)
        val chartGrowthTB = findViewById<LineChart>(R.id.chartGrowthTB)
        val chartGrowthBB = findViewById<LineChart>(R.id.chartGrowthBB)

        // ===== DATA DARI INTENT =====
        val nama = intent.getStringExtra("nama") ?: "-"
        val umur = intent.getIntExtra("umur", 0)
        val gender = intent.getStringExtra("gender") ?: "-"
        val tinggi = intent.getDoubleExtra("tinggi", 0.0)
        val berat = intent.getDoubleExtra("berat", 0.0)
        val tanggalInput = intent.getStringExtra("tanggal_input") ?: "-"

        // ===== SET TEXT =====
        tvNama.text = nama
        tvUmur.text = "Umur : $umur bulan"
        tvGender.text = "Gender : $gender"
        tvTinggi.text = "Tinggi : $tinggi cm"
        tvBerat.text = "Berat : $berat kg"
        tvTanggalInput.text = "Tanggal Input : $tanggalInput"

        // ===== HITUNG Z-SCORE =====
        val lms = getLmsWHO(gender, umur)
        val zScore = ((tinggi / lms.m).pow(lms.l) - 1) / (lms.l * lms.s)

        // ===== DATA GRAFIK =====
        val umurList = listOf(0, 1, 2, 3, 6, 9, 12)

        val zTbList = listOf(
            hitungZScore(49.9, getLmsWHO(gender, 0)),
            hitungZScore(67.6, getLmsWHO(gender, 6)),
            hitungZScore(tinggi, getLmsWHO(gender, umur)),
            hitungZScore(87.8, getLmsWHO(gender, 24))
        )

        val zBbList = listOf(0.0, -0.5, -0.3, -0.4)

        // ===== DATA TB =====
        val tbAnak = listOf(50.0, 54.5, 57.8, 61.0, 67.5, 72.0, tinggi)
        val tbMedian = listOf(49.9, 54.7, 57.7, 61.1, 67.6, 72.0, 75.7)
        val tbMinus2 = listOf(46.1, 50.8, 53.7, 56.8, 62.0, 66.5, 68.6)
        val tbMinus3 = listOf(44.2, 48.9, 51.8, 54.7, 59.8, 64.0, 66.4)

        // ===== DATA BB =====
        val bbAnak = listOf(3.3, 4.2, 5.1, 5.8, 7.5, 8.8, 9.2)
        val bbMedian = listOf(3.3, 4.5, 5.6, 6.4, 7.9, 8.9, 9.6)
        val bbMinus2 = listOf(2.5, 3.4, 4.3, 5.0, 6.4, 7.3, 7.8)
        val bbMinus3 = listOf(2.1, 2.9, 3.7, 4.3, 5.7, 6.5, 6.9)

        // ===== TAMPILKAN GRAFIK =====
        showZScoreTBChart(chartTb, umurList, zTbList)
        showZScoreBBChart(chartBb, umurList, zBbList)

        showGrowthChart(
            chartGrowthTB,
            umurList,
            tbAnak,
            tbMedian,
            tbMinus2,
            tbMinus3,
            nama,
            "Pertumbuhan Tinggi Badan (cm)"
        )

        showGrowthChart(
            chartGrowthBB,
            umurList,
            bbAnak,
            bbMedian,
            bbMinus2,
            bbMinus3,
            nama,
            "Pertumbuhan Berat Badan (kg)"
        )
    }

    // ===== UTIL =====
    private fun hitungZScore(tb: Double, lms: LmsData): Double {
        return ((tb / lms.m).pow(lms.l) - 1) / (lms.l * lms.s)
    }

    private fun getLmsWHO(gender: String, umur: Int): LmsData {
        return if (gender == "Laki-laki") {
            when (umur) {
                0 -> LmsData(1.0, 49.8842, 0.03795)
                6 -> LmsData(1.0, 67.6236, 0.03165)
                12 -> LmsData(1.0, 75.7488, 0.03137)
                24 -> LmsData(1.0, 87.8161, 0.03479)
                else -> LmsData(1.0, 75.7488, 0.03137)
            }
        } else {
            when (umur) {
                0 -> LmsData(1.0, 49.1477, 0.03790)
                6 -> LmsData(1.0, 65.7311, 0.03448)
                12 -> LmsData(1.0, 74.0150, 0.03479)
                24 -> LmsData(1.0, 86.4153, 0.03734)
                else -> LmsData(1.0, 74.0150, 0.03479)
            }
        }
    }

    private fun showZScoreTBChart(chart: LineChart, umur: List<Int>, z: List<Double>) {
        val entries = umur.zip(z).map { Entry(it.first.toFloat(), it.second.toFloat()) }
        val set = LineDataSet(entries, "Z-Score TB").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            lineWidth = 2f
            setDrawValues(false)
        }
        chart.data = LineData(set)
        setupChart(chart)
    }

    private fun showZScoreBBChart(chart: LineChart, umur: List<Int>, z: List<Double>) {
        val entries = umur.zip(z).map { Entry(it.first.toFloat(), it.second.toFloat()) }
        val set = LineDataSet(entries, "Z-Score BB").apply {
            color = Color.MAGENTA
            setCircleColor(Color.MAGENTA)
            lineWidth = 2f
            setDrawValues(false)
        }
        chart.data = LineData(set)
        setupChart(chart)
    }

    private fun showGrowthChart(
        chart: LineChart,
        umur: List<Int>,
        anak: List<Double>,
        median: List<Double>,
        minus2: List<Double>,
        minus3: List<Double>,
        labelAnak: String,
        yLabel: String
    ) {
        fun line(data: List<Double>, label: String, color: Int): LineDataSet {
            val entries = umur.zip(data).map { Entry(it.first.toFloat(), it.second.toFloat()) }
            return LineDataSet(entries, label).apply {
                this.color = color
                lineWidth = 2f
                setDrawValues(false)
            }
        }

        chart.data = LineData(
            line(anak, labelAnak, Color.BLUE),
            line(median, "Median", Color.GREEN),
            line(minus2, "-2 SD", Color.YELLOW),
            line(minus3, "-3 SD", Color.RED)
        )
        setupChart(chart)
    }

    private fun setupChart(chart: LineChart) {
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.description.isEnabled = false
        chart.invalidate()
    }
}
