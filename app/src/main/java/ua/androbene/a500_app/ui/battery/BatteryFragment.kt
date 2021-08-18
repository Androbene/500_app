package ua.androbene.a500_app.ui.battery

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import ua.androbene.a500_app.MainActivityBN
import ua.androbene.a500_app.R
import ua.androbene.a500_app.ui.memory.State

class BatteryFragment : Fragment() {
    private var mInterstitialAd: InterstitialAd? = null

    private lateinit var viewModel: BattViewModel
    private lateinit var progress: ProgressBar
    private lateinit var btn: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.batt_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(BattViewModel::class.java)
        progress = view.findViewById(R.id.progressBar)
        btn = view.findViewById(R.id.button)

        MobileAds.initialize(requireContext()) {}
        val mAdView = view?.findViewById<AdView>(R.id.adViewBattery)
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)

        viewModel.status.observe(viewLifecycleOwner) {
            when (it!!) {
                State.NOT_OPTIMIZED -> {
                    progress.visibility = View.GONE
                    btn.background = resources.getDrawable(R.drawable.button_optimize)
                    (requireActivity() as MainActivityBN).navigationEnabled = true
                }
                State.SCANNING -> {
                    progress.visibility = View.VISIBLE
                    btn.isClickable = false
                    btn.background = resources.getDrawable(R.drawable.button_scanning)
                    (requireActivity() as MainActivityBN).navigationEnabled = false
                }
                State.OPTIMIZED -> {
                    progress.visibility = View.GONE
                    btn.isClickable = true
                    btn.background = resources.getDrawable(R.drawable.button_done)
                    (requireActivity() as MainActivityBN).navigationEnabled = true
                }
                State.JOB_DONE -> {
                    progress.visibility = View.GONE
                    btn.isClickable = true
                    btn.background = resources.getDrawable(R.drawable.button_done)
                    (requireActivity() as MainActivityBN).navigationEnabled = true

                    showAd()
                    //(requireActivity() as MainActivityBN).navController?.navigate(R.id.action_memoryFragment_to_resultFragment)
                }
            }
        }

        viewModel.triggerAnimation.observe(viewLifecycleOwner) {
            val animShake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
            btn.startAnimation(animShake)
        }
        btn.setOnClickListener {
            viewModel.scan()
        }
        val batteryLevel = view.findViewById<TextView>(R.id.battery_level)
        viewModel.batteryLevel.observe(viewLifecycleOwner) {
            batteryLevel.text = "Заряд батареи: $it %"
        }
        val remainTime = view.findViewById<TextView>(R.id.remain_time)
        viewModel.remainTime.observe(viewLifecycleOwner) {
            remainTime.text = "Оставшееся время: $it"
        }
    }

    override fun onStart() {
        super.onStart()
        initAd()
        loadAd()
    }

    private fun initAd() {
        MobileAds.initialize(requireContext()) {}
    }

    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-3940256099942544/1033173712",
            //ca-app-pub-3579532281806105/8428459947 рекомендовал ГУГЛ
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(tag, adError?.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(tag, "Ad was loaded.")
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
            mInterstitialAd?.show(requireActivity())
        } else {
            Log.d(tag, "The interstitial ad wasn't ready yet.")
        }
    }
}