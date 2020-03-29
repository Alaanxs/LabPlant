package com.example.labplant

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
        const val handlerState:Int = 0
        private val recDataString:StringBuilder = StringBuilder()
        var h: Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        m_address = intent.getStringExtra(LayoutBt.EXTRA_ADDRESS)
        Log.i("mac2", m_address)
        ConnectToDevice(this).execute()

        h= object : Handler(){
            override fun handleMessage(msg: Message) {
                    if (msg.what == handlerState) {
                        val readMessage:String = msg.obj.toString()
                        Log.i("Data1 = ", readMessage)
                        val endOfLineIndex: Int = readMessage.indexOf("~")
                        if (endOfLineIndex > 0) {
                            var dataInPrint: String = readMessage.substring(0, endOfLineIndex)
                            Log.i("Data Received = ", dataInPrint)
                            var dataLength: Int = dataInPrint.length
                            if (readMessage[0] == '#'){
                                val sensor0:String = readMessage.substring(1, 3)
                                val sensor1:String = readMessage.substring(7, 9)
                                val sensor2:String = readMessage.substring(13, 15)
                                tvPHumedadS.text = sensor2 + "%"
                                tvPHumedad.text = sensor0 + "%"
                                tvCelcius.text = sensor1 + "°C"
                            }
                        }
                    }
            }
        }

        tvROn.setOnClickListener{ sendCommand("a") }
        btTime.setOnClickListener{ sendCommand("b") }
    }

     private fun sendCommand(input:String){
         val msgBuffer: ByteArray = input.toByteArray()
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(msgBuffer)
            } catch(e: IOException) {
                e.printStackTrace()
            }
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
                    readData()
                }
            } catch (e: IOException){
                connectSucces = false
                e.printStackTrace()
            }
            return null
        }

        private fun readData() {
            val buffer = ByteArray(256)
            var bytes: Int

            while (true){
                try {
                    bytes = m_bluetoothSocket!!.inputStream.read(buffer)
                    val readMessage = String(buffer, 0, bytes)
                    h?.obtainMessage(handlerState, bytes, -1, readMessage)!!.sendToTarget()
                    //Log.i("ASD", readMessage)
                } catch (e: IOException) {
                    Log.i("RD", "ERROR!")
                }
            }
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
