package com.example.camara

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.camara.ui.theme.CamaraTheme

class MainActivity : AppCompatActivity() {
    private val PERMISSION_ID = 34
    private lateinit var binding: ActivityMainBinding
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenCamera.setOnClickListener {
            if(checkCameraPermission()){
                openCamera()
            } else{
                requestPermissions()
            }
        }
    }
    private fun openCamera(){
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
    @SuppressLint("MissingSuperCall")
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if   ((grantResults.isNotEmpty()   &&   grantResults[0]   == PackageManager.PERMISSION_GRANTED)) {
                openCamera()
            } else {
                Toast.makeText(this,"Aun        requieres        permiso", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkCameraPermission(): Boolean{
        return ActivityCompat.checkSelfPermission(this,      Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),PERMISSION_ID)
    }
}
class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    var imageCapture: ImageCapture? = null
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            startCamera()
        }
        binding.captureButton.setOnClickListener {
            takePhoto()
        }
    }

    suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(this).await()
        // Construimos el preview (aquí podemos hacer configuraciones)
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
        // Seleccionamos la cámara trasera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERAtry {
            // Atamos nuestra cámara al ciclo de vida de nuestro Activity
            cameraProvider.run {
                unbindAll()
                imageCapture = ImageCapture.Builder().build()
                bindToLifecycle(this@CameraActivity, cameraSelector, preview, imageCapture)
            }
        } catch exc: Exception) {
            Toast.makeText(this, "No  se  pudo  hacer  bind  al  lifecycle", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun takePhoto() {// Creamos un nombre único para cada foto
        val format = SimpleDateFormat(
            "dd-MM-yyyyy-HH:mm:ss:SSS",
            Locale.US
        ).format(System.currentTimeMillis())
        val name = "beduPhoto $format"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/CameraX-Image"
                )// La carpeta donde se guarda
            }
        }
        //  Creamos  el  builder  para  la  configuración  del  archivo  y  los metadatos
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        // Seteamos el listener de cuando la captura sea efectuada
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                    Toast.makeText(baseContext, "Error al capturar imagen", Toast.LENGTH_SHORT)
                        .show() Log . e "CameraX", e.toString())
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        baseContext,
                        "La    imagen    ${output.savedUri}    se    guardó correctamente!",
                        Toast.LENGTH_SHORT
                    ).show() Log . d "CameraX", output.savedUri.toString())
                }
            }
        )
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CamaraTheme {
        Greeting("Android")
    }
}