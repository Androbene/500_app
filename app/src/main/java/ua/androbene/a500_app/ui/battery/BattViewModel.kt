package ua.androbene.a500_app.ui.battery

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.androbene.a500_app.ui.memory.State

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class BattViewModel(application: Application) : AndroidViewModel(application) {
    val TIME_TO_REOPTIMIZE = 60_000L

    var app: Application? = application

    val status = MutableLiveData<State>()
    val triggerAnimation = MutableLiveData(false)

    val batteryLevel = MutableLiveData(0)
    val remainTime = MutableLiveData("5h 40m")

    private var animJob: Job? = null

    init {
        Log.d("mylog", "init")
        val sharedPrefs = app!!.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val optimized = sharedPrefs.getLong("BATTERY_OPTIMIZED", 0L)
        status.value =
            if ((System.currentTimeMillis() - optimized) < TIME_TO_REOPTIMIZE) State.OPTIMIZED else State.NOT_OPTIMIZED

        if (status.value == State.NOT_OPTIMIZED) {
            animJob = startAnimate()
        }

        // BATTERY INFO
        val bm = application.getSystemService(BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        batteryLevel.value = level
    }

    private fun startAnimate(): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            do {
                delay(7000)
                triggerAnimation.postValue(triggerAnimation.value!!.not())
            } while (true)
        }
    }

    fun scan() {
        Log.d("mylog", "scan")
        viewModelScope.launch(Dispatchers.IO) {
            animJob?.cancel()
            status.postValue(State.SCANNING)
            delay(3000)
            val sharedPrefs = app!!.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putLong("BATTERY_OPTIMIZED", System.currentTimeMillis())
                .apply()
            remainTime.postValue("5h 55m")
            delay(500)
            status.postValue(State.JOB_DONE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("mylog", "app = null")
        app = null
    }
}