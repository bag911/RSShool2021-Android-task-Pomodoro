package bag.dev.rsshool2021_android_task_pomodoro

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import bag.dev.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import bag.dev.rsshool2021_android_task_pomodoro.timer.Stopwatch
import bag.dev.rsshool2021_android_task_pomodoro.timer.StopwatchAdapter
import bag.dev.rsshool2021_android_task_pomodoro.timer.StopwatchListener
import bag.dev.rsshool2021_android_task_pomodoro.methods.*


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var mainBinding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this,this)
    private val stopwatches = ArrayList<Stopwatch>()
    private var nextId = 0
    private lateinit var timerViewModel: TimerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        timerViewModel = ViewModelProvider(this).get(TimerViewModel::class.java)

        mainBinding.rcView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }


        mainBinding.editTextTimerSize.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                mainBinding.apply {
                    addNewStopwatchButton.isEnabled = editTextTimerSize.text.toString().isNotEmpty()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { return }
            override fun afterTextChanged(p0: Editable?) { return }
        })

        mainBinding.apply {
            addNewStopwatchButton.isEnabled = false
            addNewStopwatchButton.setOnClickListener {
                val aimTime = editTextTimerSize.text.toString().toLong() * 60000
                editTextTimerSize.text.clear()
                stopwatches.add(Stopwatch(nextId++, aimTime, false,0, false))
                stopwatchAdapter.submitList(stopwatches.toList())
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {

        if (timerViewModel.currentTimerId>=0){
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            val stopwatch = stopwatches[timerViewModel.currentTimerId]
            startIntent.putExtra(AIM_TIMER_TIME_MS,stopwatch.aimTime-stopwatch.currentMs)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, System.currentTimeMillis())

            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
        timerViewModel.currentTimerId = id
    }

    override fun stop(id: Int, aimTime: Long) {
        changeStopwatch(id, aimTime, false)
        timerViewModel.currentTimerId = -1
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
        timerViewModel.currentTimerId = -1
    }

    private fun changeStopwatch(id: Int, aimTime: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, aimTime ?: it.aimTime, isStarted, it.currentMs, it.isFinished))
            } else {
                newTimers.add(Stopwatch(it.id, it.aimTime, false,it.currentMs, it.isFinished))
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }



    override fun onDestroy() {
        super.onDestroy()
        onAppForegrounded()
    }
}