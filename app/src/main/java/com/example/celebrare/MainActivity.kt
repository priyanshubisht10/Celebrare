package com.example.celebrare

import android.R
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.celebrare.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fontFamilyItems = arrayOf("Arial", "SF")
        val fontFamilyAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, fontFamilyItems)
        fontFamilyAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerFontFamily.adapter = fontFamilyAdapter

        val textSizeItems = arrayOf("10", "20", "30", "40", "50")
        val textSizeAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, textSizeItems)
        textSizeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerTextSize.adapter = textSizeAdapter

        binding.imgUndo.setOnClickListener {
            binding.drawingView.undo()
        }

        binding.imgRedo.setOnClickListener {
            binding.drawingView.redo()
        }

        binding.imgSave.setOnClickListener {
            requestStoragePermission()
//            binding.drawingView.visibility = View.GONE

        }

        binding.imgAddTextBox.setOnClickListener {
            val text = binding.editText.text.toString()
            binding.textViewContainer.visibility = View.VISIBLE
            binding.drawingView.visibility = View.GONE

            binding.drawingView.addTextView(text,binding.textViewContainer)
        }

    }

//    fun addNewTextView() {
//
//        val newTextView = TextView(this)
//
//        val layoutParams = FrameLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//
//        newTextView.text = binding.editText.text.toString()
//
//        binding.textViewContainer.addView(newTextView, layoutParams)
//
//    }


    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted && (permissionName == android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Permission $permissionName granted", Toast.LENGTH_SHORT)
                        .show()
                    CoroutineScope(Dispatchers.IO).launch {
                        saveImage(getBitmapFromView(binding.drawingView))
                    }
                } else {
                    if (permissionName == android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog()
        } else {
            requestPermission.launch(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun showRationaleDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Storage Permission")
            .setMessage("Allow access to your internal storage")
            .setPositiveButton("Yes") { dialog, _ ->
                requestPermission.launch(
                    arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private suspend fun saveImage(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Images-${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/Celebrare"
            )
        }

        val contentResolver = contentResolver
        var uri: Uri? = null

        try {
            val imagesCollection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            uri = contentResolver.insert(imagesCollection, contentValues)

            uri?.let { imageUri ->
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "${imageUri.path} saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            uri?.let { imageUri ->
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri)
                sendBroadcast(mediaScanIntent)
            }
        }
    }


}
