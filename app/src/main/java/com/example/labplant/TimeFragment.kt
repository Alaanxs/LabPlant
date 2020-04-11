package com.example.labplant

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.fragment_time.*
import kotlinx.android.synthetic.main.fragment_time.view.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class TimeFragment : Fragment() {

    var date = arrayOf("día", "2 días", "3 días", "4 días", "5 días", "6 días", "7 días")
    var hora: String = ""
    var minuto: String = ""
    var ap: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vi: View = inflater.inflate(R.layout.fragment_time, container, false)

        vi.spinner.adapter = ArrayAdapter(activity!!.applicationContext, android.R.layout.simple_spinner_item, date)

        vi.timePickerA.setOnTimeChangedListener { _, hour, minute ->
            val hourA = hour
            hora = hourA.toString()
            val min = minute
            minuto = min.toString()
        }

        vi.btSaveTime.setOnClickListener{
            Toast.makeText(activity, "Time is: $hora : $minuto",Toast.LENGTH_SHORT).show()
        }

        return vi
    }

    fun sendCommand(input:String){
        val msgBuffer: ByteArray = input.toByteArray()
        if (MainActivity.m_bluetoothSocket != null) {
            try{
                MainActivity.m_bluetoothSocket!!.outputStream.write(msgBuffer)
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

}

