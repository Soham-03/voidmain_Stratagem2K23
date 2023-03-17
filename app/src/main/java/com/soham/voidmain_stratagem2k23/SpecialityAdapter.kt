package com.soham.voidmain_stratagem2k23

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class SpecialityAdapter(private val listOfSpecialities: ArrayList<Int>, private val context: Context): RecyclerView.Adapter<SpecialityAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageViewSpeciality: ImageView = itemView.findViewById(R.id.imgSpeciality)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.speciality_single_row,parent,false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val image = listOfSpecialities[position]
        holder.imageViewSpeciality.setImageResource(image)
        holder.itemView.setOnClickListener {
            val intent = Intent(context,ShowDoctors::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listOfSpecialities.size
    }

}