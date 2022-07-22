package com.chrisabbod.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.chrisabbod.happyplaces.database.DatabaseHandler
import com.chrisabbod.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.chrisabbod.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity() {

    private var binding: ActivityAddHappyPlaceBinding? = null

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    val galleryLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contentUri = result.data?.data

            try {
                val selectedImageBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)

                saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)

                Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")
                binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "Failed to load the image from the gallery",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val cameraLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val thumbnail: Bitmap = result.data?.extras?.get("data") as Bitmap

            saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)

            Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")
            binding?.ivPlaceImage?.setImageBitmap(thumbnail)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        //OnDateSetListener: used to indicate when the user has finished selecting a date
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        binding?.etDate?.setOnClickListener {
            DatePickerDialog(
                this@AddHappyPlaceActivity,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding?.tvAddImage?.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf(
                "Select photo from Gallery",
                "Capture photo from camera"
            )
            pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                when (which) {
                    0 -> choosePhotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }.show()
        }

        binding?.btnSave?.setOnClickListener {
            when {
                binding?.etTitle?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                }
                binding?.etDescription?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                }
                binding?.etLocation?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
                }
                saveImageToInternalStorage == null -> {
                    Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val happyPlaceModel = HappyPlaceModel(
                        0,
                        binding?.etTitle?.text.toString(),
                        saveImageToInternalStorage.toString(),
                        binding?.etDescription?.text.toString(),
                        binding?.etDate?.toString().toString(),
                        binding?.etLocation?.text.toString(),
                        mLatitude,
                        mLongitude
                    )
                    val dbHandler = DatabaseHandler(this)
                    val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                    if (addHappyPlace > 0) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this@AddHappyPlaceActivity).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(cameraIntent)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this@AddHappyPlaceActivity).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryLauncher.launch(galleryIntent)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this@AddHappyPlaceActivity)
            .setMessage(
                "It looks like you have turned off permissions " +
                        "required for this feature. It can be enabled under " +
                        "application settings"
            )
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView() {
        val myFormat = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}