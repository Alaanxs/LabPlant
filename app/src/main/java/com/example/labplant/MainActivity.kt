package com.example.labplant

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_time.*
import kotlinx.android.synthetic.main.fragment_time.view.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object{
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        const val handlerState:Int = 0
        var h: Handler? = null
        var stateRelay: String = "null"
        var stateRelay1: String = "null"
        var timeFormat = SimpleDateFormat("hh:mm", Locale.US)
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

                        val endOfSensors: Int = readMessage.indexOf("~")
                        val endOfStateRelay: Int = readMessage.indexOf("-")

                        if(endOfStateRelay > 0){
                            val dataRelay: Int = readMessage.indexOf("E")
                            var dataPrint: String = readMessage.substring(dataRelay, endOfStateRelay)
                            if (dataPrint[0] == 'E'){
                                stateRelay = dataPrint.substring(1,2)
                                stateRelay1 = dataPrint.substring(3,4)
                                Log.i("Data2 = ", stateRelay1)
                                when {
                                    stateRelay == "a" -> btOnR.text = "APAGAR"
                                    stateRelay == "b" -> btOnR.text = "PRENDER"
                                    else -> Toast.makeText(this@MainActivity, "Los datos del riego son incorrectos", Toast.LENGTH_LONG).show()
                                }
                                when {
                                    stateRelay1 == "a" ->{
                                        btLuzOff.isEnabled = true
                                        btLuzOn.isEnabled = false
                                    }
                                    stateRelay1 == "b" ->{
                                        btLuzOn.isEnabled = true
                                        btLuzOff.isEnabled = false
                                    }
                                    else -> Toast.makeText(this@MainActivity, "Los datos de la luz son incorrectos", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        if (endOfSensors > 0) {
                            var dataInPrint: String = readMessage.substring(0, endOfSensors)
                            Log.i("Data Received = ", dataInPrint)
                            if (readMessage[0] == '#'){
                                val sensor0:String = readMessage.substring(1, 3)
                                val sensor1:String = readMessage.substring(7, 9)
                                val sensor2:String = readMessage.substring(13, 15)
                                tvPHumedadS.text = sensor2 + "%"
                                tvPHumedad.text = sensor1 + "%"
                                tvCelcius.text = sensor0 + "°C"
                            }
                        }
                    }
            }
        }

        fabBottomBar.setOnClickListener{
            onBackPressed()
        }

        btHumR.setOnClickListener{
            loadFragment(HumidityFragment())
        }
        btOnR.setOnClickListener{
            if(stateRelay == "a"){
                sendCommand("b")
            }
            else if(stateRelay == "b"){
                sendCommand("a")
            }
        }
        btTimeR.setOnClickListener{ loadFragment(TimeFragment())
            sendCommand("a")
        }

        btLuzOn.setOnClickListener{ sendCommand("c") }
        btLuzOff.setOnClickListener{ sendCommand("d") }
        btTimeL.setOnClickListener{ loadFragment(TimeFragment()) }

        btVentiOn.setOnClickListener{ sendCommand("c") }
        btVentiOff.setOnClickListener{ sendCommand("d") }
        btTimeV.setOnClickListener{ loadFragment(TimeFragment()) }
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

    private fun loadFragment(fragment:Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun removeFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commit()
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
        }
    }

    fun obtenerHoraActual(zonaHoraria: String?): String? {
        val formato = "HH:mm"
        return obtenerFechaConFormato(formato, zonaHoraria)
    }

    fun obtenerFechaConFormato(formato: String?, zonaHoraria: String?): String? {
        val calendar = Calendar.getInstance()
        val date = calendar.time
        val sdf: SimpleDateFormat
        sdf = SimpleDateFormat(formato)
        sdf.setTimeZone(TimeZone.getTimeZone(zonaHoraria))
        return sdf.format(date)
    }

    fun relayTime(){
        val time = obtenerHoraActual("America/Mexico_City").toString()
        val hour = time.substring(0,2)
        val minute = time.substring(3,5)

        /*if (hour==hora && minute==minuto){

        }*/
    }
}
