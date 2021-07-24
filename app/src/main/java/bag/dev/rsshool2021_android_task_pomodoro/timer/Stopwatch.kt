package bag.dev.rsshool2021_android_task_pomodoro.timer

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Stopwatch(
    val id: Int,
    var aimTime: Long,
    var isStarted: Boolean,
    var currentMs: Long,
    var isFinished: Boolean
)