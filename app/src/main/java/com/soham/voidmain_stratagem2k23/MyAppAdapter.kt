package com.soham.voidmain_stratagem2k23

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAppAdapter(private var myAppointments: ArrayList<Appointment>, private var context: Context): RecyclerView.Adapter<MyAppAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_my_appointments,parent,false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val appointment = myAppointments[position]
        holder.txtDoctorName.text = appointment.doctorName
        holder.txtDoctorUid.text = "Doc Id: "+appointment.doctorUid
        holder.txtToken.text = "Token: "+appointment.token

    }

    override fun getItemCount(): Int {
        return myAppointments.size
    }

    fun updateList(list:ArrayList<Appointment>){
        myAppointments = list
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDoctorName: TextView = itemView.findViewById(R.id.txtDoctorName)
        val txtDoctorUid: TextView = itemView.findViewById(R.id.txtDocUid)
        val txtToken: TextView = itemView.findViewById(R.id.txtToken)
    }

}