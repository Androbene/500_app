package ua.androbene.a500_app.ui.splash

import android.content.Intent
import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import ua.androbene.a500_app.MainActivityBN
import ua.androbene.a500_app.R

class SplashActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private var tag = "SplashActivity"

    private val splashActivityVM: SplashActivityVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.splash)
    }

    override fun onStart() {
        super.onStart()
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        tvStatus.postDelayed(
            {
                startActivity(Intent(this, MainActivityBN::class.java))
                finish()
            }, splashActivityVM.SPLASH_TIME
        )
        val imageProgress = findViewById<ImageView>(R.id.imageProgress)
        val clippedProgress = imageProgress.drawable as ClipDrawable
        splashActivityVM.progress.observe(this) {
            clippedProgress.level = it
        }
        splashActivityVM.status.observe(this) {
            tvStatus.text = it
        }

        initAd()
        loadAd()
    }

    override fun onPause() {
        super.onPause()
        showAd()
    }

    private fun initAd() {
        MobileAds.initialize(this) {}
    }

    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(tag, adError?.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(tag, "Ad was loaded :)")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun showAd() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(tag, "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d(tag, "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(tag, "Ad showed fullscreen content.")
                mInterstitialAd = null
            }
        }

        if (mInterstitialAd != null) {
            Log.d(tag, "The interstitial ad ready.")
            mInterstitialAd?.show(this)
        } else {
            Log.d(tag, "The interstitial ad wasn't ready yet.")
        }
    }

}