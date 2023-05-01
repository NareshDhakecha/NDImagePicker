package com.ndsoftwares.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ndsoftwares.imagepicker.ext.getSerializableEx
import com.ndsoftwares.imagepicker.provider.CameraProvider
import com.ndsoftwares.imagepicker.provider.CompressionProvider
import com.ndsoftwares.imagepicker.provider.CropProvider
import com.ndsoftwares.imagepicker.provider.GalleryProvider
import com.ndsoftwares.imagepicker.util.FileUriUtils

class ImagePickerActivity : AppCompatActivity() {

    companion object{
            private const val TAG = "image_picker"

            internal fun getCancelledIntent(context: Context): Intent {
                val intent = Intent()
                val message = context.getString(R.string.error_task_cancelled)
                intent.putExtra(NDImagePicker.EXTRA_ERROR, message)
                return intent
            }
    }

    private var mGalleryProvider: GalleryProvider? = null
    private var mCameraProvider: CameraProvider? = null
    private lateinit var mCropProvider: CropProvider
    private lateinit var mCompressionProvider: CompressionProvider

    private val galleryActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result->
        mGalleryProvider?.onActivityResult(result.resultCode, result.data)
    }

    private val cameraActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result->
        mCameraProvider?.onActivityResult(result.resultCode, result.data)
    }

    private val cropActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result->
        mCropProvider.onActivityResult(result.resultCode, result.data)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadBundle(savedInstanceState)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    /**
     * Save all appropriate activity state.
     */
    public override fun onSaveInstanceState(outState: Bundle) {
        mCameraProvider?.onSaveInstanceState(outState)
        mCropProvider.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun loadBundle(savedInstanceState: Bundle?) {
        // Create Crop Provider
        mCropProvider = CropProvider(this)
        mCropProvider.onRestoreInstanceState(savedInstanceState)

        // Create Compression Provider
        mCompressionProvider = CompressionProvider(this)

        // Retrieve Image Provider
        val provider: ImageProvider? =
            intent?.extras?.let { getSerializableEx(intent.extras!!, NDImagePicker.EXTRA_IMAGE_PROVIDER, ImageProvider::class.java) }

        // Create Gallery/Camera Provider
        when (provider) {
            ImageProvider.GALLERY -> {
                mGalleryProvider = GalleryProvider(this)
                // Pick Gallery Image
                savedInstanceState ?: mGalleryProvider?.startIntent(galleryActivityResult)
            }
            ImageProvider.CAMERA -> {
                mCameraProvider = CameraProvider(this)
                mCameraProvider?.onRestoreInstanceState(savedInstanceState)
                // Pick Camera Image
                savedInstanceState ?: mCameraProvider?.startIntent(cameraActivityResult)
            }
            else -> {
                // Something went Wrong! This case should never happen
                Log.e(TAG, "Image provider can not be null")
                setError(getString(R.string.error_task_cancelled))
            }
        }
    }

    /**
     * Dispatch incoming result to the correct provider.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mCameraProvider?.onRequestPermissionsResult(requestCode)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        mCropProvider.onActivityResult(requestCode, resultCode, data)
//    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            setResultCancel()
        }

    }

    /**
     * {@link CameraProvider} and {@link GalleryProvider} Result will be available here.
     *
     * @param uri Capture/Gallery image Uri
     */
    fun setImage(uri: Uri) {
        when {
            mCropProvider.isCropEnabled() -> mCropProvider.startIntent(cropActivityResult, uri)
            mCompressionProvider.isCompressionRequired(uri) -> mCompressionProvider.compress(uri)
            else -> setResult(uri)
        }
    }

    /**
     * {@link CropProviders} Result will be available here.
     *
     * Check if compression is enable/required. If yes then start compression else return result.
     *
     * @param uri Crop image uri
     */
    fun setCropImage(uri: Uri) {
        // Delete Camera file after crop. Else there will be two image for the same action.
        // In case of Gallery Provider, we will get original image path, so we will not delete that.
        mCameraProvider?.delete()

        if (mCompressionProvider.isCompressionRequired(uri)) {
            mCompressionProvider.compress(uri)
        } else {
            setResult(uri)
        }
    }

    /**
     * {@link CompressionProvider} Result will be available here.
     *
     * @param uri Compressed image Uri
     */
    fun setCompressedImage(uri: Uri) {
        // This is the case when Crop is not enabled

        // Delete Camera file after crop. Else there will be two image for the same action.
        // In case of Gallery Provider, we will get original image path, so we will not delete that.
        mCameraProvider?.delete()

        // If crop file is not null, Delete it after crop
        mCropProvider.delete()

        setResult(uri)
    }

    // Set Result, Image is successfully capture/picked/cropped/compressed.
    private fun setResult(uri: Uri) {
        val intent = Intent()
        intent.data = uri
        intent.putExtra(NDImagePicker.EXTRA_FILE_PATH, FileUriUtils.getRealPath(this, uri))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // User has cancelled the task
    fun setResultCancel() {
        setResult(Activity.RESULT_CANCELED, getCancelledIntent(this))
        finish()
    }

    // Error occurred while processing image
    fun setError(message: String) {
        val intent = Intent()
        intent.putExtra(NDImagePicker.EXTRA_ERROR, message)
        setResult(NDImagePicker.RESULT_ERROR, intent)
        finish()
    }
}