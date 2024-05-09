package com.example.projet_kotlin_randazzo_benjamin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.projet_kotlin_randazzo_benjamin.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var movies_recycler: RecyclerView
    val db: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        updateViewFromDB()
        getPictureList()
        movies_recycler = findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = MovieAdapter(Movies(emptyList()))
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        updateViewFromDB()
        getPictureList()
        movies_recycler = findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = MovieAdapter(Movies(emptyList()))
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setAnchorView(R.id.fab).show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel"
            val descriptionText = "channel description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.first_fragment_label))
            .setContentText(getString(R.string.app_name))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.second_fragment_label)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)

        val requestPermissionLauncher =
            this.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    NotificationManagerCompat.from(this).notify(1, builder.build())
                }
            }

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                NotificationManagerCompat.from(this).notify(1, builder.build())
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    class NotificationReceiver  : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "ACTION_CLICK") {
                Toast.makeText(context, "Je suis une notification", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun getPictureList() {
        CoroutineScope(Dispatchers.IO).launch {
            requestPictureList {
                val moviesObjects = jsonToObject(it)
                db.movieDao().insertAll(*moviesObjects.movies.toTypedArray())
                //movies_recycler.adapter = MovieAdapter(Movies(db.movieDao().getAll()))
            }
        }
    }

    fun jsonToObject(json: String) : Movies{
        var gson = Gson()
        val movies = gson.fromJson(json, Movies::class.java)
        //val movies = gson.fromJson(json, Movie::class.java)
        Log.d("log_json", movies.toString())
        return movies
    }

    fun updateViewFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
            var flow = db.movieDao().getFlowData()
            flow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    movies_recycler.adapter = MovieAdapter(Movies(it))
                }
            }
        }
    }


    fun requestPictureList(callback: (String) -> Unit) {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://api.betaseries.com/movies/list")
            .get()
            .addHeader("X-BetaSeries-Key", "77b233b849ac")
            .build()

        val response: Response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        callback(responseBody)
    }


    companion object {
        const val CHANNEL_ID = "channel"
    }
}