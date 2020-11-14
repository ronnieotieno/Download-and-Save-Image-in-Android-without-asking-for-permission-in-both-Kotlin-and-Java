package com.example.saveimage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav = findViewById<TextView>(R.id.nav)

            //Navigate to java class
        nav.setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }
        //Uncomment if you want to download

//        object : Thread() {
//            override fun run() {
//                getBitmapFromURL(
//                    "https://www.roadrunnerrecords.com/sites/g/files/g2000005056/f/Sample-image10-highres.jpg"
//                )
//            }
//        }.start()

    }


    @Throws(IOException::class)
    private fun saveImage(
        context: Context,
        bitmap: Bitmap,
        folderName: String,
        fileName: String
    ): Uri? {
        val fos: OutputStream?
        var imageFile: File? = null
        var imageUri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + File.separator.toString() + folderName
            )
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir: String = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ).toString() + File.separator.toString() + folderName
            imageFile = File(imagesDir)
            if (!imageFile.exists()) {
                imageFile.mkdir()
            }
            imageFile = File(imagesDir, "$fileName.png")
            fos = FileOutputStream(imageFile)
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos?.flush()
        fos?.close()
        if (imageFile != null) // pre Q
        {
            MediaScannerConnection.scanFile(context, arrayOf(imageFile.toString()), null, null)
            imageUri = Uri.fromFile(imageFile)
        }

        runOnUiThread {
            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show()
            Log.d("SavedImage", imageUri.toString())
        }
        return imageUri
    }

    fun getBitmapFromURL(src: String?) {
        try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)

            saveImage(this, bitmap, "MyFolder", "Image${System.currentTimeMillis()}")
        } catch (e: IOException) {
            e.printStackTrace()

        }
    }
}