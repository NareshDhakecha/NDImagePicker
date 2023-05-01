package com.ndsoftwares.imagepicker.provider

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.ndsoftwares.imagepicker.ImagePickerActivity
import com.ndsoftwares.imagepicker.NDImagePicker
import com.ndsoftwares.imagepicker.R
import com.ndsoftwares.imagepicker.ext.getSerializableEx
import com.ndsoftwares.imagepicker.util.FileUtil
import com.yalantis.ucrop.UCrop
import java.io.File

class CropProvider(activity: ImagePickerActivity) : BaseProvider(activity) {

    companion object {
        private val TAG = CropProvider::class.java.simpleName

        /**
         * Key to Save/Retrieve Crop File state
         */
        private const val STATE_CROP_FILE = "state.crop_file"
    }

    private val mMaxWidth: Int
    private val mMaxHeight: Int

    private val mCrop: Boolean
    private val mCropAspectX: Float
    private val mCropAspectY: Float
    private var mCropImageFile: File? = null
    private val mFileDir: File

    init {
        val bundle = activity.intent.extras ?: Bundle()

        // Get Max Width/Height parameter from Intent
        mMaxWidth = bundle.getInt(NDImagePicker.EXTRA_MAX_WIDTH, 0)
        mMaxHeight = bundle.getInt(NDImagePicker.EXTRA_MAX_HEIGHT, 0)

        // Get Crop Aspect Ratio parameter from Intent
        mCrop = bundle.getBoolean(NDImagePicker.EXTRA_CROP, false)
        mCropAspectX = bundle.getFloat(NDImagePicker.EXTRA_CROP_X, 0f)
        mCropAspectY = bundle.getFloat(NDImagePicker.EXTRA_CROP_Y, 0f)

        // Get File Directory
        val fileDir = bundle.getString(NDImagePicker.EXTRA_SAVE_DIRECTORY)
        mFileDir = getFileDir(fileDir)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save crop file
        outState.putSerializable(STATE_CROP_FILE, mCropImageFile)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        // Restore crop file
        mCropImageFile = savedInstanceState?.let { getSerializableEx(savedInstanceState, STATE_CROP_FILE, File::class.java) }
    }

    /**
     * Check if crop should be enabled or not
     *
     * @return Boolean. True if Crop should be enabled else false.
     */
    fun isCropEnabled() = mCrop

    /**
     * Start Crop Activity
     */
    fun startIntent(cropActivityResult: ActivityResultLauncher<Intent>, uri: Uri) {
        cropImage(cropActivityResult, uri)
    }

    private fun cropImage(cropActivityResult: ActivityResultLauncher<Intent>, uri: Uri) {
        val extension = FileUtil.getImageExtension(uri)
        mCropImageFile = FileUtil.getImageFile(fileDir = mFileDir, extension = extension)

        if (mCropImageFile == null || !mCropImageFile!!.exists()) {
            Log.e(TAG, "Failed to create crop image file")
            setError(R.string.error_failed_to_crop_image)
            return
        }

        val options = UCrop.Options()
        options.setCompressionFormat(FileUtil.getCompressFormat(extension))

        val uCrop = UCrop.of(uri, Uri.fromFile(mCropImageFile))
            .withOptions(options)

        if (mCropAspectX > 0 && mCropAspectY > 0) {
            uCrop.withAspectRatio(mCropAspectX, mCropAspectY)
        }

        if (mMaxWidth > 0 && mMaxHeight > 0) {
            uCrop.withMaxResultSize(mMaxWidth, mMaxHeight)
        }

        try {
            uCrop.start(activity, cropActivityResult)
        } catch (ex: ActivityNotFoundException) {
            setError(
                "uCrop not specified in manifest file." +
                        "Add UCropActivity in Manifest" +
                        "<activity\n" +
                        "    android:name=\"com.yalantis.ucrop.UCropActivity\"\n" +
                        "    android:screenOrientation=\"portrait\"\n" +
                        "    android:theme=\"@style/Theme.AppCompat.Light.NoActionBar\"/>"
            )
            ex.printStackTrace()
        }

    }

    fun onActivityResult(resultCode: Int, data: Intent?) {
//        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                handleResult(mCropImageFile)
            } else {
                setResultCancel()
            }
//        }
    }

    private fun handleResult(file: File?) {
        if (file != null) {
            activity.setCropImage(Uri.fromFile(file))
        } else {
            setError(R.string.error_failed_to_crop_image)
        }
    }

    /**
     * Handle Crop Failed
     */
    override fun onFailure() {
        delete()
    }

    /**
     * Delete Crop File, If not required
     *
     * After Image Compression, Crop File will not required
     */
    fun delete() {
        mCropImageFile?.delete()
        mCropImageFile = null
    }
}