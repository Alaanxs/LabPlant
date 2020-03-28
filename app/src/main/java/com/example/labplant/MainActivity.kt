package com.example.labplant

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object{
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressBar
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        m_address = intent.getStringExtra(LayoutBt.EXTRA_ADDRESS)
        Log.i("mac2", m_address)
        ConnectToDevice(this).execute()

        btHume.setOnClickListener{ sendCommand("a") }
        btTime.setOnClickListener{ readData() }

    }

     private fun sendCommand(input:String){
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    open class Handler

    private fun readData(){
        val byteCount: Int = m_bluetoothSocket!!.inputStream.available()
        if (byteCount > 0) {
            val rawBytes = ByteArray(byteCount)
            m_bluetoothSocket!!.inputStream.read(rawBytes)
            val string = String(rawBytes)
            Log.i("ASD", string)
            tvhumedad.text = string
        }
    }

    private fun disconnect(){
        if(m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.close()
                m_bluetoothSocket =null
                m_isConnected = false
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>(){
        private var connectSucces: Boolean = true
        private val context: Context

        init{
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            //m_progress = ProgressBar.show(context, "Connecting…", "please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try{
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException){
                connectSucces = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSucces){
                Log.i("data", "couldn’t connect")
            } else{
                m_isConnected = true
            }
            //m_progress.dismiss()
        }
    }
}
