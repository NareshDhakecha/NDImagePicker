package com.ndsoftwares.imagepicker.sample

import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ndsoftwares.imagepicker.NDImagePicker
import com.ndsoftwares.imagepicker.sample.databinding.ActivityMainBinding
import com.ndsoftwares.imagepicker.sample.util.FileUtil
import com.ndsoftwares.imagepicker.util.IntentUtils

class MainActivity : AppCompatActivity() {
    private lateinit var bv: ActivityMainBinding

    private var mCameraUri: Uri? = null
    private var mGalleryUri: Uri? = null
    private var mProfileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bv = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bv.root)
        setSupportActionBar(bv.toolbar)

        bv.content.contentProf.imgProfile.setDrawableImage(R.drawable.ic_person, true)

        bv.content.contentProf.imgProfile.setOnClickListener {
            showImage(it)
        }
    }

    fun pickProfileImage(view: View) {
        NDImagePicker.with(this)
            .cropSquare()
            .setImageProviderInterceptor {
                Log.d("ImagePicker", "Selected ImageProvider: $it")
            }
            .setDismissListener {
                Log.d("ImagePicker", "Dialog Dismiss")
            }
            .maxResultSize(200,200)
            .start(pickProfImageActivityResult)
//        Toast.makeText(this, "pickProfileImage", Toast.LENGTH_LONG).show()
    }

    private val pickProfImageActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result->
//        result.data?.data?.let {
//            mProfileUri = it
//            mProfileUri?.let { uri-> bv.content.contentProf.imgProfile.setLocalImage(uri, true) }
//        }

        if (result.resultCode == Activity.RESULT_OK){
            // Uri object will not be null for RESULT_OK
            mProfileUri = result.data!!.data
            bv.content.contentProf.imgProfile.setLocalImage(result.data!!.data!!, false)
        }
    }

    fun pickGalleryImage(view: View) {
        NDImagePicker.with(this)
            .crop()
            .galleryOnly()
            .galleryMimeTypes(
                arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                ))
            .maxResultSize(1080,1920)
            .start(pickGalleryImageActivityResult)
    }

    private val pickGalleryImageActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result->
//        mGalleryUri = result.data?.data
//        mGalleryUri?.let { bv.content.contentGallery.imgGallery.setLocalImage(it, false) }
        if (result.resultCode == Activity.RESULT_OK){
            // Uri object will not be null for RESULT_OK
            mGalleryUri = result.data!!.data
            bv.content.contentGallery.imgGallery.setLocalImage(result.data!!.data!!, false)
        }

    }

    fun pickCameraImage(view: View) {
        NDImagePicker.with(this)
            .cameraOnly()
            // Image size will be less than 1024 KB
            // .compress(1024)
            //  Path: /storage/sdcard0/Android/data/package/files
//            .saveDir(getExternalFilesDir(null))
            //  Path: /storage/sdcard0/Android/data/package/files/DCIM
            .saveDir(getExternalFilesDir(Environment.DIRECTORY_DCIM))
            //  Path: /storage/sdcard0/Android/data/package/files/Download
//            .saveDir(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
            //  Path: /storage/sdcard0/Android/data/package/files/Pictures
//            .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
            //  Path: /storage/sdcard0/Android/data/package/files/Pictures/ImagePicker
//            .saveDir(File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ImagePicker"))
            //  Path: /storage/sdcard0/Android/data/package/files/ImagePicker
//            .saveDir(getExternalFilesDir("ImagePicker"))
            //  Path: /storage/sdcard0/Android/data/package/cache/ImagePicker
//            .saveDir(File(externalCacheDir, "ImagePicker"))
            //  Path: /data/data/package/cache/ImagePicker
//            .saveDir(File(cacheDir, "ImagePicker"))
            //  Path: /data/data/package/files/ImagePicker
//            .saveDir(File(filesDir, "ImagePicker"))

            // Below saveDir path will not work, So do not use it
            //  Path: /storage/sdcard0/DCIM
            //  .saveDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
            //  Path: /storage/sdcard0/Pictures
            //  .saveDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            //  Path: /storage/sdcard0/ImagePicker
            //  .saveDir(File(Environment.getExternalStorageDirectory(), "ImagePicker"))

            .start(pickCameraImageActivityResult)
    }

    private val pickCameraImageActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result->
        if (result.resultCode == Activity.RESULT_OK){
            // Uri object will not be null for RESULT_OK
            mCameraUri = result.data!!.data
            bv.content.contentCam.imgCamera.setLocalImage(result.data!!.data!!, false)
        }


    }

    fun showImageCode(view: View) {
        var resource = 0
        if (view === bv.content.contentProf.imgProfileCode) {
            resource = R.drawable.img_profile_code
        } else if (view === bv.content.contentCam.imgCameraCode) {
            resource = R.drawable.img_camera_code
        } else if (view === bv.content.contentGallery.imgGalleryCode) {
            resource = R.drawable.img_gallery_code
        }
        ImageViewerDialog
            .newInstance(resource)
            .show(supportFragmentManager, "")
    }

    fun showImage(view: View) {
        val uri: Uri?
        if (view === bv.content.contentProf.imgProfile) {
            uri = mProfileUri
        } else if (view === bv.content.contentCam.imgCamera) {
            uri = mCameraUri
        } else if (view === bv.content.contentGallery.imgGallery) {
            uri = mGalleryUri
        } else {
            uri = null
        }
        if (uri != null) {
            startActivity(IntentUtils.getUriViewIntent(this, uri))
        }
    }

    fun showImageInfo(view: View) {
        val uri: Uri? = if (view === bv.content.contentProf.imgProfileInfo) {
            mProfileUri
        } else if (view === bv.content.contentCam.imgCameraInfo) {
            mCameraUri
        } else if (view === bv.content.contentGallery.imgGalleryInfo) {
            mGalleryUri
        } else {
            null
        }

//        if (uri != null)
        AlertDialog.Builder(this)
            .setTitle("Image Info")
            .setMessage(FileUtil.getFileInfo(this, uri))
            .setPositiveButton("Ok", null)
            .show()
    }
}