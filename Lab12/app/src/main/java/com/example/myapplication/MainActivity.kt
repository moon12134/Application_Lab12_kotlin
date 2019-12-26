package com.example.myapplication

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button

import com.google.gson.Gson

import java.io.IOException


import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = Gson().fromJson(intent.extras!!.getString("json"), Data::class.java)
            val items = arrayOfNulls<String>(data.result!!.results!!.size)

            for (i in items.indices)
                items[i] =
                    "\n列車即將進入：" + data.result!!.results!![i].Station + "\n列車行駛目的地：" + data.result!!.results!![i].Destination
            this@MainActivity.runOnUiThread {
                AlertDialog.Builder(this@MainActivity).setTitle("台北捷運列車到站站名").setItems(items, null)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerReceiver(receiver, IntentFilter("MyMessage"))
        findViewById<Button>(R.id.btn_query).setOnClickListener(View.OnClickListener {
            val req = Request.Builder()
                .url("https://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=55ec6d6e-dc5c-4268-a725-d04cc262172b")
                .build()
            OkHttpClient().newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("查詢失敗", e.toString())
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {

                    sendBroadcast(Intent("MyMessage").putExtra("json", response.body!!.string()))
                }
            })
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    internal inner class Data {
        var result: Result? = null

        internal inner class Result {
            var results: Array<Results>? = null

            internal inner class Results {
                var Station: String? = null
                var Destination: String? = null
            }
        }
    }
}
