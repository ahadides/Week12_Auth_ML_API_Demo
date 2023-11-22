package edu.farmingdale.alrajab.week12_auth_ml_api_demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import com.google.firebase.auth.FirebaseAuth
import edu.farmingdale.alrajab.week12_auth_ml_api_demo.databinding.ActivityLandingBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LandingActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityLandingBinding
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoutBtn.setOnClickListener { logout() }
        binding.loadIamgeBtn.setOnClickListener {
            // Initializing the popup menu and giving the reference as the current context
            val popupMenu = PopupMenu(this, binding.loadIamgeBtn)

            // Inflating popup menu from popup_menu.xml file
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.camera -> {
                        openCamera()
                        true
                    }
                    R.id.upload -> {
                        openGallery()
                        true
                    }
                    else -> false
                }
            }
            // Showing the popup menu
            popupMenu.show()
        }

        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun logout() {
        firebaseAuth.signOut()
        startActivity(Intent(this@LandingActivity, LoginActivity::class.java))
        finish()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            try {
                val photoFile: File = createImageFile()
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "your.package.name.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Handle the exception, show a user-friendly message, log, etc.
                Log.e("CameraError", "Error creating image file: ${ex.message}")
            }
        } else {
            // Handle the case where no camera app is available
            Log.e("CameraError", "No camera app available")
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir("images")

        if (storageDir == null) {
            throw IOException("Failed to get external storage directory")
        }

        val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        currentPhotoPath = imageFile.absolutePath

        Log.d("ImageCreation", "Image file created: $currentPhotoPath")
        return imageFile
    }


    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    // Image captured and saved to file, use the file path or URI
                    // to load the image into your ImageView or perform any other operation
                    currentPhotoPath?.let {
                        // Do something with the photo path (e.g., load into ImageView)
                        Toast.makeText(this, "Photo saved: $it", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Capture canceled or failed
                    Toast.makeText(this, "Capture canceled or failed", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    // Handle the selected image URI from the gallery
                    val selectedImageUri = data.data
                    // Now you can use this URI to load the image ImageView
                    binding.imageUrlField.setText(data.dataString)
                    binding.imageHolder.setImageURI(selectedImageUri)
                    Toast.makeText(this, "Gallery Image selected: $selectedImageUri", Toast.LENGTH_SHORT).show()
                } else {
                    // Gallery selection canceled or failed
                    Toast.makeText(this, "Gallery selection canceled or failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 124
        private const val GALLERY_REQUEST_CODE = 123
    }
}



