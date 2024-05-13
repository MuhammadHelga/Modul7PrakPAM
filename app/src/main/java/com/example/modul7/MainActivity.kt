package com.example.modul7

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.URL
import java.util.Random
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var imgSlot1: ImageView
    private lateinit var imgSlot2: ImageView
    private lateinit var imgSlot3: ImageView
    private lateinit var btnRandom: Button
    private lateinit var outHasil: TextView

    private var isPlay = false

    private var execService1: ExecutorService? = null
    private var execService2: ExecutorService? = null
    private var execService3: ExecutorService? = null
    private var execServicePool: ExecutorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRandom = findViewById(R.id.btn_random)
        imgSlot1 = findViewById(R.id.slot1)
        imgSlot2 = findViewById(R.id.slot2)
        imgSlot3 = findViewById(R.id.slot3)
        outHasil = findViewById(R.id.tv_hasil)

        imgSlot1.setImageResource(R.drawable.bar)
        imgSlot2.setImageResource(R.drawable.bar)
        imgSlot3.setImageResource(R.drawable.bar)

        execService1 = Executors.newSingleThreadExecutor()
        execService2 = Executors.newSingleThreadExecutor()
        execService3 = Executors.newSingleThreadExecutor()
        execServicePool = Executors.newFixedThreadPool(3)

        val slotTask1 = SlotTask(imgSlot1)
        val slotTask2 = SlotTask(imgSlot2)
        val slotTask3 = SlotTask(imgSlot3)

        btnRandom.setOnClickListener {
            if (!isPlay) {
                slotTask1.reset()
                slotTask2.reset()
                slotTask3.reset()

                execServicePool?.execute(slotTask1)
                execServicePool?.execute(slotTask2)
                execServicePool?.execute(slotTask3)

                btnRandom.text = "Stop"
                isPlay = true
            } else {
                slotTask1.stop()
                slotTask2.stop()
                slotTask3.stop()

                val allSame = slotTask1.imageId == slotTask2.imageId && slotTask2.imageId == slotTask3.imageId

                if (allSame) {
                    outHasil.visibility = View.VISIBLE
                    outHasil.text = "ANDA MENANG!"
                } else {
                    outHasil.visibility = View.VISIBLE
                    outHasil.text = "ANDA KURANG BERUNTUNG"
                }

                btnRandom.text = "Gatcha Lagi"
                isPlay = false
            }
        }
    }

    internal class SlotTask(private val slotImg: ImageView) : Runnable {
        private val random = Random()
        private var arrayUrl = ArrayList<String>()

        var play = true
        var imageId: Int = -1
            private set

        override fun run() {
            try {
                while (play) {
                    val (imageUrl, id) = getRandomImage()

                    Handler(Looper.getMainLooper()).post {
                        Glide.with(slotImg.context)
                            .load(imageUrl)
                            .into(slotImg)
                    }

                    imageId = id

                    Thread.sleep(random.nextInt(500).toLong())
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        // Method to randomize the image URL
        private fun getRandomImage(): Pair<String, Int> {
            val apiUrl = "https://662e87fba7dda1fa378d337e.mockapi.io/api/v1/fruits"
            return try {
                if (arrayUrl.isEmpty()) {
                    val jsonString = URL(apiUrl).readText()
                    val jsonArray = JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        arrayUrl.add(jsonObject.getString("url"))
                    }
                }

                val randomIndex = random.nextInt(arrayUrl.size)
                val id = arrayUrl[randomIndex]
                Pair(id, randomIndex)
            } catch (e: IOException) {
                e.printStackTrace()
                Pair("", -1)
            } catch (e: JSONException) {
                e.printStackTrace()
                Pair("", -1)
            }
        }

        // Method to stop the task
        fun stop() {
            play = false
        }
        fun reset() {
            play = true
        }
    }
}
