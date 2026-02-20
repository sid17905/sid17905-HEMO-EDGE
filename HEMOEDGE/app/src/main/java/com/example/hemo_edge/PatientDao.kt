package com.example.hemo_edge
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
interface PatientDao { // Ensure this matches your package name!


    @Dao
    interface PatientDao {

        // Function 1: Add a new patient
        @Insert
        suspend fun insertPatient(patient: Patient)

        // Function 2: Get a list of all patients, newest first
        @Query("SELECT * FROM patients ORDER BY registrationDate DESC")
        suspend fun getAllPatients(): List<Patient>

        // Function 3: Delete a patient
        @Delete
        suspend fun deletePatient(patient: Patient)
    }
}