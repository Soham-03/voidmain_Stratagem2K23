package com.soham.voidmain_stratagem2k23

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soham.voidmain_stratagem2k23.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val fragmentList = arrayListOf(
            HomeFragment(),
            MyApplointmentsFragment(),
            ProfileFragment(),
        )

        val viewPagerAdapter = ViewPagerAdapter(fragmentList,this.supportFragmentManager,lifecycle)

        binding.apply{
            bottomMenu.setItemSelected(R.id.home)
            viewPagerMainActivity.adapter = viewPagerAdapter
            viewPagerMainActivity.isUserInputEnabled = false
            bottomMenu.setOnItemSelectedListener {
                when(it){
                    R.id.home->{
                        viewPagerMainActivity.currentItem = 0
                    }
                    R.id.myAppointments->{
                        viewPagerMainActivity.currentItem = 1
                    }
                    R.id.profile->{
                        viewPagerMainActivity.currentItem = 2
                    }
                }
            }
        }

        setContentView(binding.root)
    }
}