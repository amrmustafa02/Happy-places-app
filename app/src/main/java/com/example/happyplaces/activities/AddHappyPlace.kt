package com.example.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings

import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlace : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val IMAGE_DIR = "HappyPlacesImages"
    }

    /*
     prepare result launch for each activity
    */
    private var resultLauncherForCamara =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data // get data from intent
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val photoUri =
                        savePhotoOnInternalStorage((data!!.extras!!.get("data") as Bitmap))
                    binding.addImage.setImageURI(photoUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // print error message
                    Toast.makeText(
                        this@AddHappyPlace,
                        "Can not capture image from camara",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private var resultLauncherForGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    // get uri about photo
                    val photoUri = data.data

                    try {

                        // convert photo to bitmap
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)

                        // save and set photo
                        val photoUri = savePhotoOnInternalStorage(selectedImageBitmap)
                        binding.addImage.setImageURI(photoUri)
                        binding.addImage.setImageBitmap(selectedImageBitmap)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        // print error message
                        Toast.makeText(
                            this@AddHappyPlace,
                            "Can not load image from gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    private lateinit var binding: ActivityAddHappyPlaceBinding


    // make object from calender
    private var calendar = Calendar.getInstance()

    // make object from dataSetListener
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {

        // prepare design
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // prepare design toolbar
        prepareToolBar()

        // set data when activity is open
        updateTextInDateField()

        // listen items on screen
        binding.dateEdit.setOnClickListener(this)
        binding.addImageText.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            // check if click on date filed
            R.id.date_edit -> {
                showDatePicker()
            }
            R.id.add_image_text -> {
                showChooseImageDialog()
            }
        }
    }

    private fun prepareToolBar() {
        setSupportActionBar(binding.toolBarAddPlace)

        // show back icon in tool bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // back to last screen
        binding.toolBarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun showDatePicker() {
        // set date on today
        dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // put (updateText) here to change time up to date
            updateTextInDateField()
        }

        // make object from data picker and set its style
        val date = DatePickerDialog(
            this@AddHappyPlace,
            R.style.datePicker,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // show dialog
        date.show()

        /*
        change color of cancel button and ok button
        but must change that after show
         */
        date.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        date.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#2A4D69"))

    }

    private fun updateTextInDateField() {
        // what format i want
        val format = "dd/MM/yyyy"

        // make object from date format
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)

        /*
        get time from calender and convert it to string
        and set text in date field
         */
        val text = sdf.format(calendar.time).toString()
        binding.dateEdit.setText(text)
    }

    private fun showChooseImageDialog() {
        // build dialog on current screen
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Choose Action")

        // prepare dialog items
        val items = arrayOf("Select image from Gallery", "Capture photo from Camara")
        dialog.setItems(items) { _, which ->
            // check which choose user is submit
            when (which) {
                0 -> {
                    choosePhotoFromGallery()
                }
                1 -> {
                    capturePhotoFromCamera()
                }
            }
        }
        dialog.show()
    }

    private fun capturePhotoFromCamera() {

        Dexter.withContext(this).withPermission(Manifest.permission.CAMERA).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    openCaptureCamara()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    showSettingsDialogAndGoIt()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showSettingsDialogAndGoIt()
                }

            }
        ).onSameThread().check()

    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                showSettingsDialogAndGoIt()
            }

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    openGallery()
                }
            }
        }).onSameThread().check()
    }

    private fun openCaptureCamara() {
        val camaraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncherForCamara.launch(camaraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"

        resultLauncherForGallery.launch(galleryIntent)
    }

    private fun showSettingsDialogAndGoIt() {
        AlertDialog.Builder(this)
            .setMessage("It looks you have turned off permissions required")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                    //set data in screen
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri

                    //start activity
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@AddHappyPlace, "Error", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancel") { diao, _ ->
                diao.dismiss()
            }.show()
    }

    private fun savePhotoOnInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)

        // Context.MODE_PRIVATE -> make files access only by application
        var file = wrapper.getDir(IMAGE_DIR, Context.MODE_PRIVATE)

        //  UUID.randomUUID() -> give to image unique id
        // and store the image by format {jpg}
        file = File(file, "${UUID.randomUUID()}.jpg")

        /*
        now we try to output image in device
         */
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // clear and close stream
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // return file by format Uri
        return Uri.parse(file.absolutePath)

    }


}

