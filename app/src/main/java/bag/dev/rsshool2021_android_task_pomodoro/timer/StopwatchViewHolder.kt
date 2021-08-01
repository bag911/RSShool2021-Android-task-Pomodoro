package bag.dev.rsshool2021_android_task_pomodoro.timer

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import bag.dev.rsshool2021_android_task_pomodoro.R
import bag.dev.rsshool2021_android_task_pomodoro.databinding.StopwatchItemBinding
import kotlinx.coroutines.*

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    private val scope = CoroutineScope(Dispatchers.Main + CoroutineName("TimeScope"))
    private var job:Job? = null

    fun bind(stopwatch: Stopwatch) {
        binding.textView.text = (stopwatch.aimTime-stopwatch.currentMs).displayTime()
        binding.customView.setPeriod(stopwatch.aimTime)
        binding.customView.setCurrent(stopwatch.currentMs)
        if (stopwatch.aimTime <= stopwatch.currentMs)
            stopwatch.isFinished = true
        removeBtn(stopwatch)
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer()
        }
        initButtonsListeners(stopwatch)
    }

    private fun coroutineTimer(stopwatch: Stopwatch){
        job?.cancel()
        job = scope.launch(Dispatchers.Main) {
            while (true){
                stopwatch.currentMs += 1000L
                binding.textView.text = (stopwatch.aimTime - stopwatch.currentMs).displayTime()
                binding.customView.setCurrent(stopwatch.currentMs)
                if (stopwatch.aimTime <= stopwatch.currentMs){
                    stopTimer()
                    stopwatch.isFinished = true
                    Toast.makeText(context, "Timer finished", Toast.LENGTH_SHORT).show()
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseBtn.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id,stopwatch.aimTime)
            } else{
                listener.start(stopwatch.id)
            }
        }

        binding.deleteBtn.setOnClickListener {
            listener.delete(stopwatch.id)
            job?.cancel()
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseBtn.text = context.getString(R.string.stopBtn)
        coroutineTimer(stopwatch)
        binding.indicator.isInvisible = false
        (binding.indicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        binding.startPauseBtn.text = context.getString(R.string.startBtn)

        job?.cancel()
        binding.indicator.isInvisible = true
        (binding.indicator.background as? AnimationDrawable)?.stop()
    }

    private fun removeBtn(stopwatch: Stopwatch){
        binding.startPauseBtn.isEnabled = !stopwatch.isFinished
    }


    private fun Long.displayTime(): CharSequence {

        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"

    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }
}