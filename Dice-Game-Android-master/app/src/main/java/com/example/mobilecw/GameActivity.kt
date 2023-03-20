package com.example.mobilecw

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import java.util.*

class GameActivity : AppCompatActivity() {

    private lateinit var diceImagesPlayer: Array<ImageView> // ImageView arrays for the dice images of both comp and the player
    private lateinit var diceImagesComp: Array<ImageView>
    private var playerSelectedDice = IntArray(5) // An array of indices of dice that user choose not to re-roll
    private var compSelectedDice = IntArray(5) // An array of indices of dice that comp choose not to re-roll
    private val rand = Random()
    private var playerRandArray = IntArray(5) // An array of random integers assigned to the dice
    private var compRandArray = IntArray(5) // An array of random integers assigned to the dice
    private var playerTotal = 0 // Total score
    private var compTotal = 0
    private var playerRerollCount = 0 // Re-roll count
    private val winScore = 101
    private var throwPressed = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.game_page)

        // Initialize checkboxes, text views and buttons
        val check1 = findViewById<CheckBox>(R.id.checkDice1)
        val check2 = findViewById<CheckBox>(R.id.checkDice2)
        val check3 = findViewById<CheckBox>(R.id.checkDice3)
        val check4 = findViewById<CheckBox>(R.id.checkDice4)
        val check5 = findViewById<CheckBox>(R.id.checkDice5)
        val compTotText = findViewById<TextView>(R.id.compScore)
        val playerTotText = findViewById<TextView>(R.id.playerScore)
        val rollButton = findViewById<Button>(R.id.throw_btn)
        val scoreButton = findViewById<Button>(R.id.score_btn)
        val reButton = findViewById<Button>(R.id.reroll_btn)
        val winsText = findViewById<TextView>(R.id.hText)

        // Using a new application subclass that saves the player wins and comp wins
        // Reference - https://stackoverflow.com/questions/4208886/using-the-android-application-class-to-persist-data
        val savedData = applicationContext as SaveData
        winsText.text = "H:${savedData.humanWins} / C:${savedData.compWins}" // Display the win count

        // Make the score and re-roll buttons invisible until the dice are thrown
        scoreButton.visibility = View.INVISIBLE
        reButton.visibility = View.INVISIBLE

        // Display the player and comp scores
        compTotText.text = compTotal.toString()
        playerTotText.text = playerTotal.toString()

        // Initialize player and comp dice images
        // Image reference - https://game-icons.net/1x1/delapouite/dice-six-faces-four.html#download
        diceImagesPlayer = arrayOf(
            findViewById(R.id.player1),
            findViewById(R.id.player2),
            findViewById(R.id.player3),
            findViewById(R.id.player4),
            findViewById(R.id.player5)
        )

        diceImagesComp = arrayOf(
            findViewById(R.id.comp1),
            findViewById(R.id.comp2),
            findViewById(R.id.comp3),
            findViewById(R.id.comp4),
            findViewById(R.id.comp5)
        )

        // Make the checkboxes invisible after the dice are thrown
        check1.visibility = View.INVISIBLE
        check2.visibility = View.INVISIBLE
        check3.visibility = View.INVISIBLE
        check4.visibility = View.INVISIBLE
        check5.visibility = View.INVISIBLE

        // An array is used to store the dice that the player select and choose not to re-roll
        check1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                playerSelectedDice[0]=1
            } else {
                playerSelectedDice[0]=0
            }
        }
        check2.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                playerSelectedDice[1]=2
            }else {
                playerSelectedDice[1]=0
            }
        }
        check3.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                playerSelectedDice[2]=3
            }
            else {
                playerSelectedDice[2]=0
            }
        }
        check4.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                playerSelectedDice[3]=4
            }else {
                playerSelectedDice[3]=0
            }
        }
        check5.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                playerSelectedDice[4]=5
            }else {
                playerSelectedDice[4]=0
            }
        }

        // Using savedInstanceState to restore the variables if the orientation changes
        if (savedInstanceState != null) {
            playerTotal = savedInstanceState.getInt("playerTot")
            compTotal = savedInstanceState.getInt("compTot")
            compTotText.text = compTotal.toString()
            playerTotText.text = playerTotal.toString()
            playerRerollCount = savedInstanceState.getInt("rerollCount")
            val resPlayerImgArray = savedInstanceState.getIntArray("playerImgArray")
            if (resPlayerImgArray != null) {
                for (i in resPlayerImgArray.indices) {
                    val drawableId = when (resPlayerImgArray[i]) {
                        1 -> R.drawable.dice_one
                        2 -> R.drawable.dice_two
                        3 -> R.drawable.dice_three
                        4 -> R.drawable.dice_four
                        5 -> R.drawable.dice_five
                        else -> R.drawable.dice_six
                    }
                    playerRandArray[i] = resPlayerImgArray[i]
                    diceImagesPlayer[i].setImageResource(drawableId)
                }
            }
            val resCompImgArray = savedInstanceState.getIntArray("compImgArray")
            if (resCompImgArray != null) {
                for (i in resCompImgArray.indices) {
                    val drawableId = when (resCompImgArray[i]) {
                        1 -> R.drawable.dice_one
                        2 -> R.drawable.dice_two
                        3 -> R.drawable.dice_three
                        4 -> R.drawable.dice_four
                        5 -> R.drawable.dice_five
                        else -> R.drawable.dice_six
                    }
                    compRandArray[i] = resCompImgArray[i]
                    diceImagesComp[i].setImageResource(drawableId)
                }
            }
            // Use a boolean value to know if the score button is pressed in order to make visible the needed buttons
            val throwPress = savedInstanceState.getBoolean("throwPressed")
            if (throwPress) {
                rollButton.visibility = View.INVISIBLE
                reButton.visibility = View.VISIBLE
                scoreButton.visibility = View.VISIBLE
                check1.visibility = View.VISIBLE
                check2.visibility = View.VISIBLE
                check3.visibility = View.VISIBLE
                check4.visibility = View.VISIBLE
                check5.visibility = View.VISIBLE
            }
        }

        // Set an onclick listener to the throw button
        rollButton.setOnClickListener {
            rollDicePlayer() // Functions to roll the player dice
            rollDiceComp() // Functions to roll the comp dice
            // Make visible the necessary elements
            scoreButton.visibility = View.VISIBLE
            reButton.visibility = View.VISIBLE
            check1.visibility = View.VISIBLE
            check2.visibility = View.VISIBLE
            check3.visibility = View.VISIBLE
            check4.visibility = View.VISIBLE
            check5.visibility = View.VISIBLE
            rollButton.visibility = View.INVISIBLE
            throwPressed = true
        }

        // Set an onclick listener to the score button
        scoreButton.setOnClickListener {
            check1.visibility = View.INVISIBLE
            check2.visibility = View.INVISIBLE
            check3.visibility = View.INVISIBLE
            check4.visibility = View.INVISIBLE
            check5.visibility = View.INVISIBLE

            // Initiate the comp strategy to select and re-roll the dice that the computer wants
            for (i in 0..1) {
                rerollComp(compSelectedDice)
            }

            // Update the score of the player and comp after the score button in clicked
            for (i in 0..4) {
                playerTotal += playerRandArray[i]
                compTotal += compRandArray[i]
                compTotText.text = compTotal.toString()
                playerTotText.text = playerTotal.toString()
                scoreButton.visibility = View.INVISIBLE
                reButton.visibility = View.INVISIBLE
                // Make the re-roll count zero
                playerRerollCount = 0
            }
            rollButton.visibility = View.VISIBLE
            // Display the win or lose popup
            displayWinner()
            throwPressed = false
        }

        // Set an onclick listener to the re-roll button
        reButton.setOnClickListener {
            // Check and re-roll if the player has enough re-rolls left
            if (playerRerollCount<=1){
                playerRerollCount+=1
                reroll(playerSelectedDice)
            } else {
                // Display a toast message and stop the re-rolls
                Toast.makeText(applicationContext, "All re-rolls have been used, "
                        + "score will be updated automatically", Toast.LENGTH_LONG).show()
                // Automatically update the score after the re-rolls are used
                for (i in 0..1) {
                    rerollComp(compSelectedDice)
                }
                check1.visibility = View.INVISIBLE
                check2.visibility = View.INVISIBLE
                check3.visibility = View.INVISIBLE
                check4.visibility = View.INVISIBLE
                check5.visibility = View.INVISIBLE
                for (i in 0..4) {
                    playerTotal += playerRandArray[i]
                    compTotal += compRandArray[i]
                    compTotText.text = compTotal.toString()
                    playerTotText.text = playerTotal.toString()
                    scoreButton.visibility = View.INVISIBLE
                    reButton.visibility = View.INVISIBLE
                    playerRerollCount = 0
                }
                displayWinner()
                rollButton.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun displayWinner() { // Function to display the pop-up dialog of the win or loss

        val savedData = applicationContext as SaveData // Update the wins saved in the application subclass (SaveData.class)
        if ((playerTotal >= winScore) && (playerTotal != winScore) && (playerTotal > compTotal)) {

            /* Check if the user reaches the win score, if there's a tie this function
            will not run until player or comp reaches a higher score
             */

            // Dialog boxes for the win or lose popups
            // Reference - https://www.youtube.com/watch?v=ukL6oURCAq4
            val dialogBinding = layoutInflater.inflate(R.layout.you_win,null)

            val dialogBox = Dialog(this)
            dialogBox.setContentView(dialogBinding)

            dialogBox.setCancelable(true)
            dialogBox.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogBox.setCanceledOnTouchOutside(false)
            dialogBox.show()

            val okBtn = dialogBinding.findViewById<Button>(R.id.okBtn)
            okBtn.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            // Incerease the player win count in the application sub class
            savedData.humanWins++

        } else if ((compTotal>=winScore) && (compTotal!=winScore)) {

            val dialogBinding = layoutInflater.inflate(R.layout.you_lose,null)

            val dialogBox = Dialog(this)
            dialogBox.setContentView(dialogBinding)

            dialogBox.setCancelable(true)
            dialogBox.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogBox.setCanceledOnTouchOutside(false)
            dialogBox.show()

            val okBtn = dialogBinding.findViewById<Button>(R.id.okBtn)
            okBtn.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            savedData.compWins++
        }
    }

    private fun reroll(selectedDice : IntArray) { // Function to re-roll the dice not selected by the user

        for (i in diceImagesPlayer.indices) {

            // Check if the index is in the selectedDice array initialized earlier
            if (i+1  in selectedDice) {
            } else{
                // Generate a random number between 1 and 6
                val num = rand.nextInt(6) + 1

                // Update the image for the die
                val drawableId = when (num) {
                    1 -> R.drawable.dice_one
                    2 -> R.drawable.dice_two
                    3 -> R.drawable.dice_three
                    4 -> R.drawable.dice_four
                    5 -> R.drawable.dice_five
                    else -> R.drawable.dice_six
                }
                playerRandArray[i] = num // Update the random index array
                // Use an animation for the dice
                // Reference - https://www.youtube.com/watch?v=cQl2cRYggGs
                val anim = AnimationUtils.loadAnimation(this, R.anim.dice_roll_animation) // load the dice roll animation
                diceImagesPlayer[i].startAnimation(anim) // start the animation on the current dice ImageView
                diceImagesPlayer[i].setImageResource(drawableId) // Set the dice to the imageview
            }

        }
    }

    private fun rerollComp(selectedDice: IntArray) { // Strategy used by the computer for the re-roll

        // Calculate the score accumulated from the throw
        var compScore = 0
        for (i in compRandArray.indices) {
            compScore += compRandArray[i]
        }

        // Take 20 as the average amount and only re-roll the dice if the score is less than 20
        if (compScore < 20) {
            /* Check each index of the compRandArray which are the random numbers allocated for the image-views of the dice
            If the selected index has a value greater than or equal to 4 add it to the compSelectedArray which
            will leave these dice as it is and rotate the remaining dice */
            if ((compRandArray[0] >= 4)) {
                compSelectedDice[0] = 1
            }
            if ((compRandArray[1] >= 4)) {
                compSelectedDice[1] = 2
            }
            if ((compRandArray[2] >= 4)) {
                compSelectedDice[2] = 3
            }
            if ((compRandArray[3] >= 4)) {
                compSelectedDice[3] = 4
            }
            if ((compRandArray[4] >= 4)) {
                compSelectedDice[4] = 5
            }

            /* Loop through the image-view array, if the index is in the selected dice array,
            Randomly allocate dice values again to the non selected index in the array. */
            for (i in diceImagesComp.indices) {

                if (i+1  in selectedDice) {
                } else{
                    // Generate a random number between 1 and 6
                    val num = rand.nextInt(6) + 1

                    // Update the image for the die
                    val drawableId = when (num) {
                        1 -> R.drawable.dice_one
                        2 -> R.drawable.dice_two
                        3 -> R.drawable.dice_three
                        4 -> R.drawable.dice_four
                        5 -> R.drawable.dice_five
                        else -> R.drawable.dice_six
                    }
                    compRandArray[i] = num
                    val anim = AnimationUtils.loadAnimation(this, R.anim.dice_roll_animation) // load the dice roll animation
                    diceImagesComp[i].startAnimation(anim) // start the animation on the current dice ImageView
                    diceImagesComp[i].setImageResource(drawableId)
                }
            }
        }
    }

    private fun rollDicePlayer() { // Function to generate random numbers and update the dice for player
        for (i in diceImagesPlayer.indices) {
            // Generate a random number between 1 and 6
            val num = rand.nextInt(6) + 1

            // Update the image for the die
            val drawableId = when (num) {
                1 -> R.drawable.dice_one
                2 -> R.drawable.dice_two
                3 -> R.drawable.dice_three
                4 -> R.drawable.dice_four
                5 -> R.drawable.dice_five
                else -> R.drawable.dice_six
            }
            playerRandArray[i] = num
            val anim = AnimationUtils.loadAnimation(this, R.anim.dice_roll_animation) // load the dice roll animation
            diceImagesPlayer[i].startAnimation(anim) // start the animation on the current dice ImageView
            diceImagesPlayer[i].setImageResource(drawableId)
        }
    }

    private fun rollDiceComp() { // Function to generate random numbers and update the dice for comp
        for (i in diceImagesComp.indices) {
            // Generate a random number between 1 and 6
            val num = rand.nextInt(6) + 1

            // Update the image for the die
            val drawableId = when (num) {
                1 -> R.drawable.dice_one
                2 -> R.drawable.dice_two
                3 -> R.drawable.dice_three
                4 -> R.drawable.dice_four
                5 -> R.drawable.dice_five
                else -> R.drawable.dice_six
            }
            compRandArray[i] = num
            val anim = AnimationUtils.loadAnimation(this, R.anim.dice_roll_animation) // load the dice roll animation
            diceImagesComp[i].startAnimation(anim) // start the animation on the current dice ImageView
            diceImagesComp[i].setImageResource(drawableId)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) { // On save Instance method has been overridden to save the necessary data
        super.onSaveInstanceState(outState)
        outState.putInt("playerTot",playerTotal)
        outState.putInt("compTot",compTotal)
        outState.putIntArray("playerImgArray",playerRandArray)
        outState.putIntArray("compImgArray",compRandArray)
        outState.putBoolean("throwPressed",throwPressed)
        outState.putInt("rerollCount",playerRerollCount)
    }

}



