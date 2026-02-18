package com.example.hemo_edge


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

// --- TENSORFLOW IMPORTS (These were missing!) ---
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType
class MainActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var txtResult: TextView
    private lateinit var cameraExecutor: ExecutorService
    // ... existing variables ...
    private lateinit var interpreter: Interpreter
    private val MODEL_PATH = "hemo_edge_quantized.tflite"
    // ...
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.viewFinder)
        txtResult = findViewById(R.id.txtResult)

        // Check Camera Permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    try {
        interpreter = Interpreter(loadModelFile(MODEL_PATH))
    } catch (e: Exception) {
        e.printStackTrace()
    }}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Image Analyzer (The AI Part)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        processImage(imageProxy)
                    })
                }

            // Select Back Camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {

        // SAFETY CHECK: If model didn't load, close frame and exit
        if (!::interpreter.isInitialized) {
            imageProxy.close()
            return
        }

        try {
            // 1. Convert to Bitmap
            val bitmap = imageProxy.toBitmap()

            if (bitmap != null) {
                // 2. Prepare Image (Resize to 224x224)
                val imageProcessor = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                    .build()

                var tImage = TensorImage(DataType.UINT8)
                tImage.load(bitmap)
                tImage = imageProcessor.process(tImage)

                // 3. Output Buffer
                val probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.UINT8)

                // 4. Run AI
                interpreter.run(tImage.buffer, probabilityBuffer.buffer.rewind())

                // 5. Interpret (0-255 -> 0-1)
                val result = probabilityBuffer.floatArray[0] / 255.0f
                val isNormal = result > 0.5
                val confidence = if (isNormal) result * 100 else (1 - result) * 100

                // 6. Update Screen
                runOnUiThread {
                    val color = if (isNormal) 0xFF00FF00.toInt() else 0xFFFF0000.toInt()
                    val label = if (isNormal) "NORMAL CELL" else "LEUKEMIA SUSPECTED"

                    txtResult.text = "$label\nConfidence: ${"%.1f".format(confidence)}%"
                    txtResult.setTextColor(color)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        } finally {
            // CRITICAL: Always close the frame!
            imageProxy.close()
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "HemoEdge"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    private fun loadModelFile(path: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(path)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
