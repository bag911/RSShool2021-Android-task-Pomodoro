package bag.dev.rsshool2021_android_task_pomodoro.timer

interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, aimTime: Long)


    fun delete(id: Int)
}