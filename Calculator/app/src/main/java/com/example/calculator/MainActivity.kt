package com.example.calculator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonExit.setOnClickListener {
            moveTaskToBack(true)
            exitProcess(-1)
        }
        buttonAbout.setOnClickListener {
            val about = Intent(this, About::class.java)
            startActivity(about)
        }
        buttonSimple.setOnClickListener {
            val simple = Intent(this, CalcSimple::class.java)
            startActivity(simple)
        }
        buttonSci.setOnClickListener {
            val sci = Intent(this, CalcSci::class.java)
            startActivity(sci)
        }
    }

}
