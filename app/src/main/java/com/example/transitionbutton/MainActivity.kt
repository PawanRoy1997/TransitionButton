package com.example.transitionbutton

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.mylibrary.TransitionButton
import com.example.mylibrary.utils.WindowUtils
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowUtils.makeStatusbarTransparent(this)
        supportActionBar!!.hide()

        val transitionShakeButton = findViewById<TransitionButton>(R.id.transitionShakeBtn)
        transitionShakeButton.setOnClickListener {
            transitionShakeButton.startAnimation()

            Handler().postDelayed({
                transitionShakeButton.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE, null)
            }, 2000)
        }

        val transitionExpandButton = findViewById<TransitionButton>(R.id.transitionExpandBtn)
        transitionExpandButton.setOnClickListener {
            transitionExpandButton.startAnimation()

            Handler().postDelayed({
                transitionExpandButton.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND, null)
            }, 2000)
        }
    }
}