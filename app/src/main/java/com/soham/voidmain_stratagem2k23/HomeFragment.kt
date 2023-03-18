package com.soham.voidmain_stratagem2k23

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soham.voidmain_stratagem2k23.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var specialityAdapter: SpecialityAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: BookAppointmentAdapter
    private lateinit var listOfDoctors: ArrayList<Doctor>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        listOfDoctors = ArrayList()
        if (auth.currentUser == null) {
            val intent = Intent(requireActivity(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            db.collection("patients").document(auth.currentUser!!.uid).get()
                .addOnSuccessListener {
                    binding.txtUserName.text = it.get("name").toString()
                }
            val list = arrayListOf(
                R.drawable.speciality1,
                R.drawable.speciality2,
                R.drawable.speciality3,
                R.drawable.speciality4,
                R.drawable.speciality1
            )
            specialityAdapter = SpecialityAdapter(list, requireActivity())
            binding.recyclerViewProblems.layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.HORIZONTAL, false
            )
            binding.recyclerViewProblems.adapter = specialityAdapter
            adapter = BookAppointmentAdapter(listOfDoctors, requireActivity())
            binding.recyclerViewAvailableDoctors.layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.recyclerViewAvailableDoctors.adapter = adapter
            db.collection("doctors")
                .orderBy("name")
                .addSnapshotListener{value,error->
                    for (doc in value!!) {
                        listOfDoctors.add(
                            Doctor(
                                doc.id,
                                doc["name"].toString(),
                                doc["speciality"].toString(),
                                10,
                                doc["daysOfTheWeek"].toString(),
                                doc["roomNo"].toString()
                            )
                        )
                        adapter.updateList(listOfDoctors)
                    }
                }
        }
        binding.btnEmergency.setOnClickListener {
            try {
                val my_callIntent = Intent(Intent.ACTION_DIAL)
                my_callIntent.data = Uri.parse("tel:+9188888888")
                //here the word 'tel' is important for making a call...
                startActivity(my_callIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    requireActivity(),
                    "Error in your phone call" + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return binding.root
    }
}
