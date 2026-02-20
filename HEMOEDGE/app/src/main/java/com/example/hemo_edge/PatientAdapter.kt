package com.example.hemo_edge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PatientAdapter(
    private var patientList: List<Patient>,
    private val onAnalyzeClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvPatientDetails)
        val btnAnalyze: Button = itemView.findViewById(R.id.btnAnalyze)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientList[position]

        holder.tvName.text = "${patient.firstName} ${patient.lastName}"
        holder.tvDetails.text = "Age: ${patient.age} | Gender: ${patient.gender}"

        holder.btnAnalyze.setOnClickListener {
            onAnalyzeClick(patient)
        }
    }

    override fun getItemCount(): Int = patientList.size

    fun updateData(newList: List<Patient>) {
        patientList = newList
        notifyDataSetChanged()
    }
}