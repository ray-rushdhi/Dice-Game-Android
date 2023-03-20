package com.example.mobilecw

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Background image reference
        super.onCreate(savedInstanceState)
        // Hide the action bar on the top of the layout
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        // Create variables for all the buttons on the main layout
        val aboutBtn = findViewById<Button>(R.id.about_btn)
        val gameBtn = findViewById<Button>(R.id.start_btn)
        val animImage = findViewById<ImageView>(R.id.animView)
        // Function for the dice roll animation
        rollDice(animImage)

        gameBtn.setOnClickListener {
            // Starting a new activity on the click of the button
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        aboutBtn.setOnClickListener {
            // Displaying the pop-up dialog
            val dialogBinding = layoutInflater.inflate(R.layout.about,null)

            val dialogBox = Dialog(this)
            dialogBox.setContentView(dialogBinding)

            dialogBox.setCancelable(true)
            dialogBox.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogBox.show()

            val okBtn = dialogBinding.findViewById<Button>(R.id.okBtn)
            okBtn.setOnClickListener {
                dialogBox.dismiss()
            }
        }
    }

    private fun rollDice(dice: ImageView) { // Reference - https://stackoverflow.com/questions/50780400/how-to-make-dice-roll-animation-like-ludo-game
        val anim = ValueAnimator.ofInt(0, 6) // create a ValueAnimator to animate between 0 and 5
        anim.repeatCount = ValueAnimator.INFINITE // set the repeat count to infinite
        anim.duration = 1000 // set the duration of each animation cycle to 1 second
        anim.addUpdateListener { valueAnimator ->
            val drawableRes = when (valueAnimator.animatedValue as Int) {
                0 -> R.drawable.anim1
                1 -> R.drawable.anim2
                2 -> R.drawable.anim3
                3 -> R.drawable.anim4
                4 -> R.drawable.anim5
                5 -> R.drawable.anim6
                else -> R.drawable.anim1// default to dice 1 if the value is out of range
            }
            dice.setImageResource(drawableRes) // set the image resource of the dice ImageView
        }
        anim.start() // start the animation
    }

}