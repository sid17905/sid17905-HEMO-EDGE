package com.example.hemo_edge

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var patientAdapter: PatientAdapter
    private lateinit var rvPatients: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)
        rvPatients = findViewById(R.id.rvPatients)
        val fabAddPatient = findViewById<FloatingActionButton>(R.id.fabAddPatient)

        // Setup the list and the Analyze Button Click behavior
        setupRecyclerView()

        // Load existing patients from the database when the app opens
        loadPatients()

        fabAddPatient.setOnClickListener {
            showAddPatientDialog()
        }
    }

    private fun setupRecyclerView() {
        patientAdapter = PatientAdapter(emptyList()) { selectedPatient ->
            // THIS happens when the "Analyze" button is clicked!
            Toast.makeText(this, "Starting Analysis for ${selectedPatient.firstName}...", Toast.LENGTH_SHORT).show()
        }
        rvPatients.layoutManager = LinearLayoutManager(this)
        rvPatients.adapter = patientAdapter
    }

    private fun loadPatients() {
        CoroutineScope(Dispatchers.IO).launch {
            val patientsFromDb = database.patientDao().getAllPatients()
            withContext(Dispatchers.Main) {
                patientAdapter.updateData(patientsFromDb)
            }
        }
    }

    private fun showAddPatientDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_patient, null)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etFirstName)
        val etLastName = dialogView.findViewById<EditText>(R.id.etLastName)
        val etAge = dialogView.findViewById<EditText>(R.id.etAge)
        val etGender = dialogView.findViewById<EditText>(R.id.etGender)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val firstName = etFirstName.text.toString()
                val lastName = etLastName.text.toString()
                val ageText = etAge.text.toString()
                val gender = etGender.text.toString()

                if (firstName.isNotEmpty() && lastName.isNotEmpty() && ageText.isNotEmpty()) {
                    val newPatient = Patient(
                        firstName = firstName,
                        lastName = lastName,
                        age = ageText.toInt(),
                        gender = gender
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        database.patientDao().insertPatient(newPatient)
                        // After saving, reload the list so the new patient appears instantly!
                        loadPatients()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Patient Added!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}