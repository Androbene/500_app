package ua.androbene.a500_app.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class SplashActivityVM : ViewModel() {
    val SPLASH_TIME = 8500L
    val progress = MutableLiveData(0)
    val status = MutableLiveData("")

    init {
        viewModelScope.launch(Dispatchers.Default) {
            startProgress()
        }
    }

    private suspend fun startProgress() {
        var i = 0
        while (i < SPLASH_TIME) {
            i += 70
            progress.postValue(i)
            when (i) {
                in 0..2000 -> status.postValue("Loading...")
                in 2000..4000 -> status.postValue("Setting configurations...")
                in 4000..8000 -> status.postValue("Analyzing...")
            }
            delay(100)
        }
    }

}