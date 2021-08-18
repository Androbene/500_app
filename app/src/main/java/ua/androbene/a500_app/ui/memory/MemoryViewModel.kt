package ua.androbene.a500_app.ui.memory

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MemoryViewModel(application: Application) : AndroidViewModel(application) {
    val TIME_TO_REOPTIMIZE = 60_000L

    var app: Application? = application

    val status = MutableLiveData<State>()
    val triggerAnimation = MutableLiveData(false)

    val totalMemory = MutableLiveData(0)
    private var garbageNOpt = 0
    private var garbageOpt = 0
    val garbage = MutableLiveData(0)

    private var animJob: Job? = null

    init {
        Log.d("mylog", "init")
        val sharedPrefs = app!!.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val optimized = sharedPrefs.getLong("MEMORY_OPTIMIZED", 0L)
        status.value =
            if ((System.currentTimeMillis() - optimized) < TIME_TO_REOPTIMIZE) State.OPTIMIZED else State.NOT_OPTIMIZED

        if (status.value == State.NOT_OPTIMIZED) {
            animJob = startAnimate()
            garbageNOpt = Random.nextInt(100, 200)
            garbageOpt = Random.nextInt(20, 50)
            garbage.value = garbageNOpt
        } else {
            garbageNOpt = sharedPrefs.getInt("MEMORY_OPTIMIZED_VAL", 0)
            garbageOpt = garbageNOpt
            garbage.value = garbageNOpt
        }

        // TOTAL MEMORY  INFO
        val mi = ActivityManager.MemoryInfo()
        val activityManager = application.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        val totalMemoryMB = mi.totalMem / 1048576L
        totalMemory.value = totalMemoryMB.toInt()
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
            delay(300)
            for (i in garbageNOpt downTo garbageOpt step 5) {
                delay(100)
                garbage.postValue(i)
            }
            garbageNOpt = garbageOpt

            val sharedPrefs = app!!.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putLong("MEMORY_OPTIMIZED", System.currentTimeMillis())
                .putInt("MEMORY_OPTIMIZED_VAL", garbageOpt)
                .apply()
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

enum class State {
    NOT_OPTIMIZED,
    SCANNING,
    OPTIMIZED,
    JOB_DONE
}