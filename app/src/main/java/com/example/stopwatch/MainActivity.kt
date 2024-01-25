package com.example.stopwatch

import android.icu.number.Precision
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.stopwatch.databinding.ActivityMainBinding
import java.text.MessageFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var precisionSpinner: Spinner
    private lateinit var stopwatchTextView: TextView
    private lateinit var startPauseButton: Button
    private lateinit var resetButton: Button

    private var isTimerRunning = false
    private var timeInSecond=0
    private var seconds = 0
    private var minutes = 0
    private var hours = 0
    private var milliSeconds = 0
    private var millisecondTime: Long = 0
    private var startTime: Long = 0
    private var timeBuff: Long = 0
    private var updateTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val runnableMilli: Runnable = object : Runnable {
        override fun run() {
            millisecondTime = SystemClock.uptimeMillis() - startTime
            updateTime = timeBuff + millisecondTime
            seconds = (updateTime / 1000).toInt()
            minutes = seconds / 60
            hours = minutes / 60
            seconds %= 60
            minutes %= 60
            milliSeconds = (updateTime % 1000).toInt()

            binding.stopwatchTextView.text = MessageFormat.format(
                "{0}:{1}:{2}:{3}",
                hours,
                String.format(Locale.getDefault(), "%02d", minutes),
                String.format(Locale.getDefault(), "%02d", seconds),
                String.format(Locale.getDefault(), "%03d", milliSeconds)
            )
            handler.postDelayed(this, 0)
        }
    }
    private var precision = Precision.SECONDS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        precisionSpinner = findViewById(R.id.precisionSpinner)
        stopwatchTextView = findViewById(R.id.stopwatchTextView)
        startPauseButton = findViewById(R.id.startPauseButton)
        resetButton = findViewById(R.id.resetButton)

        setupPrecisionSpinner()
        setupStartPauseButton()
        setupResetButton()
    }

    private fun setupPrecisionSpinner() {
        val precisionAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.precision,
            R.layout.drop_down_item
        )
        precisionAdapter.setDropDownViewResource(R.layout.drop_down_item)
        precisionSpinner.adapter = precisionAdapter

        precisionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedPrecision = parentView?.getItemAtPosition(position).toString()
                precision = if (selectedPrecision.equals("MILLISECONDS", true)) {
                    binding.stopwatchTextView.text = "00:00:00:000"
                    Precision.MILLISECONDS
                } else {
                    binding.stopwatchTextView.text = "00:00:00"
                    Precision.SECONDS
                }
                if (isTimerRunning) {
                    stopTimer()
                    startTimer()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupStartPauseButton() {
        startPauseButton.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                startTimer()
            }
        }
    }

    private fun setupResetButton() {
        resetButton.setOnClickListener {
            stopTimer()
            millisecondTime = 0
            startTime = 0
            timeBuff = 0
            updateTime = 0
            seconds = 0
            minutes = 0
            hours = 0
            milliSeconds = 0

            timeInSecond=0
            if(precision== Precision.SECONDS){
            binding.stopwatchTextView.text = "00:00:00"
            }else binding.stopwatchTextView.text = "00:00:00:000"
        }
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            runnable = object : Runnable {
                override fun run() {

                    if(precision== Precision.SECONDS){
                        timeInSecond++
                        val timeFormat = formatTimeSecond(timeInSecond)
                        binding.stopwatchTextView.text = timeFormat
                        handler.postDelayed(this,1000)
                    }
                }
            }
            handler.postDelayed(runnable,1000)
            if(precision==Precision.MILLISECONDS){
                startTime = SystemClock.uptimeMillis()
                handler.postDelayed(runnableMilli, 0)
            }
            isTimerRunning = true
            startPauseButton.text = "Pause"
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            timeBuff += millisecondTime
            handler.removeCallbacksAndMessages(null)
            isTimerRunning = false
            startPauseButton.text = "Start"
        }
    }


    private fun formatTimeSecond(timeSeconds: Int): String {
        val hours=timeSeconds/3600
        val minutes=(timeSeconds %3600)/60
        val seconds=timeSeconds%60

        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    enum class Precision {
        SECONDS, MILLISECONDS
    }

}
