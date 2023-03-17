package com.soham.voidmain_stratagem2k23

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookAppointmentAdapter(private var listOfDoctors: ArrayList<Doctor>, private var context: Context): RecyclerView.Adapter<BookAppointmentAdapter.MyViewHolder>() {
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.single_row_book_appoint_doctor,parent,false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val doctor = listOfDoctors[position]
        println(doctor.speciality)
        holder.txtDoctorName.text = doctor.name
        holder.txtSpeciality.text = doctor.speciality
        holder.txtAvailableSlots.text = doctor.appointmentsRemaining.toString()
        holder.txtDaysWorking.text = doctor.daysOfTheWeek
        holder.btnBookAppointment.setOnClickListener {
            if(doctor.appointmentsRemaining<1){
                Toast.makeText(context, "No Slots left for today :(", Toast.LENGTH_SHORT).show()
            }
            else{
                val map = HashMap<String,Any>()
                map["doctorName"] = doctor.name
                map["doctorId"] = doctor.id
                db.collection("appointments").document(user!!.uid)
                    .collection("my appointments").document().set(map)
                    .addOnSuccessListener {
                        val remainingAppointments = doctor.appointmentsRemaining-1
                        db.collection("doctors").document(doctor.id).update("appointmentsRemaining",remainingAppointments.toLong())
                            .addOnSuccessListener {
                                Toast.makeText(context, "Your Appointment has been fixed", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to book appointment", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfDoctors.size
    }

    fun updateList(newList: ArrayList<Doctor>){
        listOfDoctors = newList
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDoctorName: TextView = itemView.findViewById(R.id.txtDoctorName)
        val txtSpeciality: TextView = itemView.findViewById(R.id.txtSpeciality)
        val txtDaysWorking: TextView = itemView.findViewById(R.id.txtDaysAvailable)
        val txtAvailableSlots: TextView = itemView.findViewById(R.id.txtRemainingAppoint)
        val btnBookAppointment: MaterialButton = itemView.findViewById(R.id.btnBookAppointment)
    }

}