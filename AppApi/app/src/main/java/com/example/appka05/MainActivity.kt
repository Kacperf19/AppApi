package com.example.appka05

import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.appka05.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.cityTV.text = ""
        binding.tempTV.text = ""
        binding.descTV.text = ""

        var location = Location("me")
        location.latitude = 50.016361
        location.longitude = 22.674770

        CoroutineScope(Dispatchers.Main).launch {
            val myForecast = currentForecast(location)
            val temp = myForecast?.main?.temp
            val city = myForecast?.name
            val desc = myForecast?.weather?.get(0)?.description
            val icon = myForecast?.weather?.get(0)?.icon
            binding.cityTV.text = city
            binding.tempTV.text = "$temp °C"
            binding.descTV.text = desc
            val iconURL = "https://openweathermap.org/img/wn/$icon@2x.png"
            Glide.with(this@MainActivity)
                .load(iconURL)
                .into(binding.iconIV)
        }
    }
    private suspend fun currentForecast(location: Location) : Forecast? {
        val lat = location.latitude
        val lon = location.longitude

        val url = "https://api.openweathermap.org/data/2.5/weather?lang=pl&units=metric&lat=$lat&lon=$lon&appid=e3f53346b71e14905fc96af13e04c340"
        val httpClient = OkHttpClient()
        val httpRequest = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful){
                throw IOException("Error: $response")
            }
            val body = response.body!!.string()
            val gsonParser = GsonParser()
            val currentWeather = gsonParser.getForecast(body)
            currentWeather
        }
    }
}
class GsonParser {
    private val gson = Gson()

    fun getForecast(json : String) : Forecast {
        return gson.fromJson(json, Forecast::class.java)
    }
}

