package com.soham.voidmain_stratagem2k23

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soham.voidmain_stratagem2k23.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var specialityAdapter: SpecialityAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        if(auth.currentUser==null){
            val intent = Intent(this@MainActivity,SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        else{
            db.collection("patients").document(auth.currentUser!!.uid).get()
                .addOnSuccessListener {
                binding.txtUserName.text = it.get("name").toString()
            }
            val list = arrayListOf<Int>(
                R.drawable.speciality1,
                R.drawable.speciality1,
                R.drawable.speciality1,
                R.drawable.speciality1
            )
            specialityAdapter = SpecialityAdapter(list,this@MainActivity)
            binding.recyclerViewProblems.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
            binding.recyclerViewProblems.adapter = specialityAdapter
        }
        setContentView(binding.root)
    }
}