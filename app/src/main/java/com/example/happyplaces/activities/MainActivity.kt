package com.example.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // get design
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // go to add place screen bu intent
        binding.fabBtn.setOnClickListener {
            val intent = Intent(this, AddHappyPlace::class.java)
            startActivity(intent)
        }


    }
}