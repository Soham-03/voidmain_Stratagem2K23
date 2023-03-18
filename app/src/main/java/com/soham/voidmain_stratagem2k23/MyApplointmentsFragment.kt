package com.soham.voidmain_stratagem2k23

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.soham.voidmain_stratagem2k23.databinding.FragmentHomeBinding
import com.soham.voidmain_stratagem2k23.databinding.FragmentMyApplointmentsBinding

class MyApplointmentsFragment : Fragment() {
    private lateinit var binding: FragmentMyApplointmentsBinding
    private lateinit var adapter: MyAppAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyApplointmentsBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val list = ArrayList<Appointment>()
        adapter = MyAppAdapter(list,requireActivity())
        binding.recyclerViewMyAppointments.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        binding.recyclerViewMyAppointments.adapter = adapter
        db.collection("appointments").document(user!!.uid)
            .collection("my appointments").orderBy("doctorName").addSnapshotListener{value,error->
                for(doc in value!!){
                    list.add(Appointment(doc["doctorName"].toString(),doc["doctorId"].toString(),doc["token"].toString()))
                }
                adapter.updateList(list)
            }

        return binding.root
    }
}