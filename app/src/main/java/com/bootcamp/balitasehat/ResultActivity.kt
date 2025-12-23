package com.bootcamp.balitasehat

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bootcamp.balitasehat.model.LmsData
import kotlin.math.pow
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import android.graphics.Color


class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val fromHistory = intent.getBooleanExtra("from_history", false)
        val chartTb = findViewById<LineChart>(R.id.chartTb)
        val chartBb = findViewById<LineChart>(R.id.chartBb)

        val nama = intent.getStringExtra("nama") ?: ""
        val umur = intent.getIntExtra("umur", 0)
        val gender = intent.getStringExtra("gender") ?: ""
        val tinggi = intent.getDoubleExtra("tinggi", 0.0)
        val tanggalInput = intent.getStringExtra("tanggal_input")
            ?: if (fromHistory) "-" else "-"

        val lms = getLmsWHO(gender, umur)
        val zScore = ((tinggi / lms.m).pow(lms.l) - 1) / (lms.l * lms.s)

        val status = when {
            zScore < -2 -> "Stunting"
            zScore < -1 -> "Berisiko Stunting"
            else -> "Normal"
        }

        tvResult.text = """
            Nama Anak     : $nama
            Umur          : $umur bulan
            Gender        : $gender
            Tinggi Badan  : $tinggi cm
            Tanggal Input : $tanggalInput

            L = ${lms.l}
            M = ${lms.m}
            S = ${lms.s}

            Z-Score TB/U  : ${"%.2f".format(zScore)}
            Status        : $status
        """.trimIndent()



        val zTbList = listOf(
            hitungZScore(49.9, getLmsWHO(gender, 0)),
            hitungZScore(67.6, getLmsWHO(gender, 6)),
            hitungZScore(tinggi, getLmsWHO(gender, umur)),
            hitungZScore(87.8, getLmsWHO(gender, 24))
        )

        val zBbList = listOf(0.0, -0.5, -0.3, -0.4)

        val umurList = listOf(0, 1, 2, 3, 6, 9, 12)

// TB (cm)
        val tbAnak = listOf(50.0, 54.5, 57.8, 61.0, 67.5, 72.0, 75.6)
        val tbMedian = listOf(49.9, 54.7, 57.7, 61.1, 67.6, 72.0, 75.7)
        val tbMinus2 = listOf(46.1, 50.8, 53.7, 56.8, 62.0, 66.5, 68.6)
        val tbMinus3 = listOf(44.2, 48.9, 51.8, 54.7, 59.8, 64.0, 66.4)

// BB (kg)
        val bbAnak = listOf(3.3, 4.2, 5.1, 5.8, 7.5, 8.8, 9.2)
        val bbMedian = listOf(3.3, 4.5, 5.6, 6.4, 7.9, 8.9, 9.6)
        val bbMinus2 = listOf(2.5, 3.4, 4.3, 5.0, 6.4, 7.3, 7.8)
        val bbMinus3 = listOf(2.1, 2.9, 3.7, 4.3, 5.7, 6.5, 6.9)

        showZScoreTBChart(chartTb, umurList, zTbList)
        showZScoreBBChart(chartBb, umurList, zBbList)

        val chartGrowthTB = findViewById<LineChart>(R.id.chartGrowthTB)
        val chartGrowthBB = findViewById<LineChart>(R.id.chartGrowthBB)

        showGrowthChart(
            chartGrowthTB,
            umurList,
            tbAnak,
            tbMedian,
            tbMinus2,
            tbMinus3,
            "Budi",
            "Pertumbuhan Tinggi Badan (cm)"
        )

        showGrowthChart(
            chartGrowthBB,
            umurList,
            bbAnak,
            bbMedian,
            bbMinus2,
            bbMinus3,
            "Budi",
            "Pertumbuhan Berat Badan (kg)"
        )

        findViewById<TextView>(R.id.tvTitleZTb).text =
            "Grafik Z-Score Tinggi Badan (TB/U) - $nama ($gender)"

        findViewById<TextView>(R.id.tvTitleZBb).text =
            "Grafik Z-Score Berat Badan (BB/U) - $nama ($gender)"

        findViewById<TextView>(R.id.tvTitleGrowthTb).text =
            "Grafik Pertumbuhan Tinggi Badan - $nama"

        findViewById<TextView>(R.id.tvTitleGrowthBb).text =
            "Grafik Pertumbuhan Berat Badan - $nama"

    }

    private fun hitungZScore(tb: Double, lms: LmsData): Double {
        return ((tb / lms.m).pow(lms.l) - 1) / (lms.l * lms.s)
    }

    /**
     * Data L, M, S diambil dari WHO Child Growth Standards
     * Length-for-age 0–24 bulan
     */
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

    private fun showZScoreBBChart(chart: LineChart, umur: List<Int>, z: List<Double>) {

        val zEntries = umur.zip(z).map {
            Entry(it.first.toFloat(), it.second.toFloat())
        }

        val zSet = LineDataSet(zEntries, "Z-Score BB").apply {
            color = Color.MAGENTA
            setCircleColor(Color.MAGENTA)
            circleRadius = 4f
            lineWidth = 2f
            setDrawValues(false)
        }

        val medianSet = constantLine(umur, 0f, "Median (0)", Color.GREEN)
        val minus2Set = constantLine(umur, -2f, "-2 SD", Color.parseColor("#FFA500"))
        val minus3Set = constantLine(umur, -3f, "-3 SD", Color.RED)
        val plus2Set  = constantLine(umur, 2f, "+2 SD", Color.BLUE)

        chart.data = LineData(zSet, medianSet, minus2Set, minus3Set, plus2Set)

        setupZChart(chart)
    }

    private fun showZScoreTBChart(chart: LineChart, umur: List<Int>, z: List<Double>) {

        val zEntries = umur.zip(z).map {
            Entry(it.first.toFloat(), it.second.toFloat())
        }

        val zSet = LineDataSet(zEntries, "Z-Score TB").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            lineWidth = 2f
            setDrawValues(false)
        }

        val medianSet = constantLine(umur, 0f, "Median (0)", Color.GREEN)
        val minus2Set = constantLine(umur, -2f, "-2 SD", Color.parseColor("#FFA500"))
        val minus3Set = constantLine(umur, -3f, "-3 SD", Color.RED)

        chart.data = LineData(zSet, medianSet, minus2Set, minus3Set)

        setupZChart(chart)
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
        fun line(data: List<Double>, label: String, color: Int, dashed: Boolean = false)
                : LineDataSet {

            val entries = umur.zip(data).map {
                Entry(it.first.toFloat(), it.second.toFloat())
            }

            return LineDataSet(entries, label).apply {
                this.color = color
                lineWidth = 2f
                setDrawCircles(!dashed)
                setDrawValues(false)
                if (dashed) enableDashedLine(10f, 5f, 0f)
            }
        }

        val anakSet = line(anak, labelAnak, Color.BLUE)
        val medianSet = line(median, "WHO Median (SD 0)", Color.GREEN, true)
        val minus2Set = line(minus2, "-2 SD", Color.parseColor("#FFA500"), true)
        val minus3Set = line(minus3, "-3 SD", Color.RED, true)

        chart.data = LineData(anakSet, medianSet, minus2Set, minus3Set)

        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.description.text = yLabel
        chart.invalidate()
    }


    private fun constantLine(
        umur: List<Int>,
        value: Float,
        label: String,
        color: Int
    ): LineDataSet {

        val entries = umur.map {
            Entry(it.toFloat(), value)
        }

        return LineDataSet(entries, label).apply {
            this.color = color
            lineWidth = 1.5f
            setDrawCircles(false)
            enableDashedLine(10f, 5f, 0f)
            setDrawValues(false)
        }
    }

    private fun setupZChart(chart: LineChart) {
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.axisLeft.axisMinimum = -4f
        chart.axisLeft.axisMaximum = 4f
        chart.description.isEnabled = false
        chart.invalidate()
    }


}
