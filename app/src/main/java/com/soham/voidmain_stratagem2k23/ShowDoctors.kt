package com.soham.voidmain_stratagem2k23

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Orientation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.soham.voidmain_stratagem2k23.databinding.ActivityShowDoctorsBinding

class ShowDoctors : AppCompatActivity() {
    private lateinit var binding: ActivityShowDoctorsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var listOfDoctors: ArrayList<Doctor>
    private lateinit var adapter: BookAppointmentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowDoctorsBinding.inflate(layoutInflater)
        db = FirebaseFirestore.getInstance()
        listOfDoctors = ArrayList()
        val speciality = intent.getStringExtra("speciality")
        db.collection("doctors").whereEqualTo("speciality",speciality)
            .get()
            .addOnSuccessListener {
                for (doc in it) {
                    listOfDoctors.add(
                        Doctor(
                            doc.id,
                            doc["name"].toString(),
                            doc["speciality"].toString(),
                            doc["appointmentsRemaining"].toString().toInt(),
                            doc["daysOfTheWeek"].toString(),
                            doc["roomNo"].toString()
                        )
                    )
                }
                adapter = BookAppointmentAdapter(listOfDoctors,this@ShowDoctors)
                binding.txtDoctorsList.layoutManager = LinearLayoutManager(this@ShowDoctors,LinearLayoutManager.VERTICAL,false)
                binding.txtDoctorsList.adapter = adapter
                println(listOfDoctors)
            }

        setContentView(binding.root)
    }
}