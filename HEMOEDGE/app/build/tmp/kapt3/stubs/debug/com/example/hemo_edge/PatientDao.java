package com.example.hemo_edge;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00050\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u000b"}, d2 = {"Lcom/example/hemo_edge/PatientDao;", "", "deletePatient", "", "patient", "Lcom/example/hemo_edge/Patient;", "(Lcom/example/hemo_edge/Patient;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllPatients", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertPatient", "app_debug"})
@androidx.room.Dao()
public abstract interface PatientDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertPatient(@org.jetbrains.annotations.NotNull()
    com.example.hemo_edge.Patient patient, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM patients ORDER BY registrationDate DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllPatients(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.hemo_edge.Patient>> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePatient(@org.jetbrains.annotations.NotNull()
    com.example.hemo_edge.Patient patient, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}