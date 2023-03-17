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

        db.collection("doctors")
            .get()
            .addOnCompleteListener { p0 ->
                if (p0.isSuccessful) {
                    for (doc in p0.result) {
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
                    adapter.notifyDataSetChanged()
                    binding.txtDoctorsList.layoutManager = LinearLayoutManager(this@ShowDoctors,LinearLayoutManager.VERTICAL,false)
                    binding.txtDoctorsList.adapter = adapter
                    println(listOfDoctors)
                } else {
                    Toast.makeText(this@ShowDoctors, "Failed to retrieve Data", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        setContentView(binding.root)
    }
}