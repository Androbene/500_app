package ua.androbene.a500_app

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivityBN : AppCompatActivity() {
    private var tag = "MainActivityBN"
    var navController: NavController? = null
    var navigationEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main_b_n)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        navView.setOnNavigationItemSelectedListener { item ->
            if (navigationEnabled) {
                when (item.itemId) {
                    R.id.itemMemory -> {
                        try {
                            navController?.navigate(R.id.action_battFragment_to_memoryFragment)
                        } catch (e: Exception) {
                            Log.d(tag, e.message.toString())
                        }
                        true
                    }
                    R.id.itemBatt -> {
                        try {
                            navController?.navigate(R.id.action_memoryFragment_to_battFragment)
                        } catch (e: Exception) {
                            Log.d(tag, e.message.toString())
                        }
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    }

    override fun onBackPressed() {
        // костыль
        val adb = AlertDialog.Builder(this)
        var alertDialog: AlertDialog? = null
        val dialogView = layoutInflater.inflate(R.layout.exit_dialog, null)
        dialogView.findViewById<ImageButton>(R.id.button_optimize).setOnClickListener {
            try {
                navController?.navigate(R.id.action_battFragment_to_memoryFragment)
            } catch (e: Exception) {
                Log.d(tag, e.message.toString())
            }
            alertDialog?.cancel()
        }
        adb.setMessage("Вы действительно хотите выйти?")
        adb.setView(dialogView)
        adb.setPositiveButton("Выход") { _: DialogInterface, i: Int ->
            finish()
        }

        adb.setNegativeButton("Оптимизироваь") { _: DialogInterface, i: Int ->
            try {
                navController?.navigate(R.id.action_memoryFragment_to_battFragment)
            } catch (e: Exception) {
                Log.d(tag, e.message.toString())
            }
        }
        alertDialog = adb.show()

    }
}

