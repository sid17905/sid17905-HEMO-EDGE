package com.example.hemo_edge

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val patientId: Long = 0,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val gender: String,
    val registrationDate: Long = System.currentTimeMillis()
)