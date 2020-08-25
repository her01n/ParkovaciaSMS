package org.heroin.parkovaciasms

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener,
    TextWatcher {

    private lateinit var ecv: EditText
    private lateinit var city: Spinner
    private lateinit var zone: Spinner
    private lateinit var send: Button
    private lateinit var sendHint: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)
        ecv = findViewById<EditText>(R.id.ecv)
        city = findViewById<Spinner>(R.id.city)
        zone = findViewById<Spinner>(R.id.zone)
        send = findViewById<Button>(R.id.send)
        sendHint = findViewById<TextView>(R.id.send_hint)
        ecv.addTextChangedListener(this)
        city.onItemSelectedListener = this
        send.setOnClickListener(this)
    }

    private var zoneAdapterResourceId = -1

    override fun onPause() {
        // TOOD create json file to store this state
        File(filesDir, "ecv.txt").writeText(ecv.text.toString())
        File(filesDir, "city.txt").writeText(city.selectedItem.toString())
        File(filesDir, "zone.txt").writeText(zone.selectedItem.toString())
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
        zoneAdapterResourceId = -1
        try {
            ecv.setText(File(filesDir, "ecv.txt").readText())
            selectItem(city, File(filesDir, "city.txt").readText())
            refresh()
            selectItem(zone, File(filesDir, "zone.txt").readText())
        } catch (e: IOException) {
            // ignored
        }
    }

    private fun selectItem(spinner: Spinner, text: String) {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i) == text) {
                spinner.setSelection(i)
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {
        refresh()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        refresh()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        refresh()
    }

    override fun onClick(parent: View?) {
        ecv.setText(ecv.text.toString().toUpperCase())
        val m = Regex("([A-Z][A-Z])[ -.,]*(\\d\\d\\d)[ -,.]*([A-Z][A-Z])").find(ecv.text)
        if (m != null) {
            ecv.setText(m.groupValues[1] + m.groupValues[2] + m.groupValues[3])
        }
        if (!Regex("[A-Z][A-Z]\\d\\d\\d[A-Z][A-Z]").matches(ecv.text.toString())) {
            AlertDialog.Builder(this)
                .setTitle("Formát EČV")
                .setMessage(String.format("EČV '%s' má asi nesprávny formát. Pokračovať?", ecv.text))
                .setPositiveButton(
                    android.R.string.ok
                ) { dialog, which -> continueSend() }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        } else {
            continueSend()
        }
    }

    private fun continueSend() {
        val selected = city.selectedItem
        if (selected.equals("Banská Bystrica")) {
            sendSMS("2200", String.format("BB A1 %s", ecv.text))
        } else if (selected.equals("Bratislava")) {
            sendSMS("2200", String.format("BA A4 %s", ecv.text))
        } else if (selected.equals("Brezno")) {
            sendSMS("2200", String.format("%s %s 1", zone.selectedItem, ecv.text))
        } else if (selected.equals("Nová Baňa")) {
            sendSMS("2200", String.format("NB %s 1", ecv.text))
        }
    }

    private fun refresh() {
        val selected = city.selectedItem
        if (selected != null) {
            if (selected.equals("Banská Bystrica")) {
                loadZones(R.array.no_zone)
            } else if (selected.equals("Brezno")) {
                loadZones(R.array.br_zones)
            } else if (selected.equals("Bratislava")) {
                loadZones(R.array.no_zone)
            } else if (selected.equals("Nová Baňa")) {
                loadZones(R.array.no_zone)
            }
            if (zone.selectedItem == null) zone.setSelection(0)
            if (!ecv.text.toString().equals("")) {
                send.isEnabled = true;
                sendHint.setText("")
            } else {
                send.isEnabled = false
                sendHint.setText("Vyplňte EČV")
            }
        } else {
            send.isEnabled = false
            sendHint.setText("Vyberte Mesto")
        }
    }

    private fun loadZones(id: Int) {
        if (zoneAdapterResourceId != id) {
            val adapter =
                ArrayAdapter.createFromResource(this, id, android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            zone.adapter = adapter
            zoneAdapterResourceId = id
        }
    }

    private fun sendSMS(number: String, text: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null))
        intent.putExtra("sms_body", text)
        startActivity(intent)
    }

}
