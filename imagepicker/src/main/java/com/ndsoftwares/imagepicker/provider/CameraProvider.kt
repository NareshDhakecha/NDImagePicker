package com.ndsoftwares.imagepicker.provider

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.ndsoftwares.imagepicker.ImagePickerActivity
import com.ndsoftwares.imagepicker.NDImagePicker
import com.ndsoftwares.imagepicker.R
import com.ndsoftwares.imagepicker.ext.getSerializableEx
import com.ndsoftwares.imagepicker.util.FileUtil
import com.ndsoftwares.imagepicker.util.IntentUtils
import com.ndsoftwares.imagepicker.util.PermissionUtil
import java.io.File

class CameraProvider(activity: ImagePickerActivity) : BaseProvider(activity) {

    companion object {
        // Key to Save/Retrieve Camera File state
        private const val STATE_CAMERA_FILE = "state.camera_file"

        // Permission Require for Image Capture using Camera
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )

        private const val PERMISSION_INTENT_REQ_CODE = 4282
    }

    private lateinit var cameraActivityResult: ActivityResultLauncher<Intent>

    // Temp Camera File
    private var mCameraFile: File? = null

    // Camera image will be stored in below file directory
    private val mFileDir: File

    init {
        val bundle = activity.intent.extras ?: Bundle()

        // Get File Directory
        val fileDir = bundle.getString(NDImagePicker.EXTRA_SAVE_DIRECTORY)
        mFileDir = getFileDir(fileDir)
    }

    /**
    * Save CameraProvider state

    * mCameraFile will lose its state when activity is recreated on
    * Orientation change or for Low memory device.
    *
    * Here, We Will save its state for later use
    *
    * Note: To produce this scenario, enable "Don't keep activities" from developer options
    **/
    override fun onSaveInstanceState(outState: Bundle) {
        // Save Camera File
        outState.putSerializable(STATE_CAMERA_FILE, mCameraFile)
    }

    /**
     * Retrieve CameraProvider state
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        // Restore Camera File
//        mCameraFile = savedInstanceState?.getSerializable(STATE_CAMERA_FILE, null) as File?
        mCameraFile = savedInstanceState?.let { getSerializableEx(savedInstanceState, STATE_CAMERA_FILE, File::class.java) }
    }

    /**
     * Start Camera Intent
     *
     * Create Temporary File object and Pass it to Camera Intent
     */
    fun startIntent(cameraActivityResult: ActivityResultLauncher<Intent>) {
        if (!IntentUtils.isCameraAppAvailable(this)) {
            setError(R.string.error_camera_app_not_found)
            return
        }

        this.cameraActivityResult = cameraActivityResult
        checkPermission()
    }

    /**
     * Check Require permission for Taking Picture.
     *
     * If permission is not granted request Permission, Else start Camera Intent
     */
    private fun checkPermission() {
        if (isPermissionGranted(this)) {
            // Permission Granted, Start Camera Intent
            startCameraIntent()
        } else {
            // Request Permission
            requestPermission()
        }
    }

    /**
     * Start Camera Intent
     *
     * Create Temporary File object and Pass it to Camera Intent
     */
    private fun startCameraIntent() {
        // Create and get empty file to store capture image content
        val file = FileUtil.getImageFile(fileDir = mFileDir)
        mCameraFile = file

        // Check if file exists
        if (file != null && file.exists()) {
            val cameraIntent = IntentUtils.getCameraIntent(this, file)
//            activity.startActivityForResult(cameraIntent, CAMERA_INTENT_REQ_CODE)
            cameraActivityResult.launch(cameraIntent)
        } else {
            setError(R.string.error_failed_to_create_camera_image_file)
        }
    }

    /**
     * Request Runtime Permission required for Taking Pictures.
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity,
            getRequiredPermission(activity),
            PERMISSION_INTENT_REQ_CODE
        )
    }

    /**
     * Check if require permission granted for Taking Picture.
     *
     * @param context Application Context
     * @return boolean true if all required permission granted else false.
     */
    private fun isPermissionGranted(context: Context): Boolean {
        return getRequiredPermission(context).none {
            !PermissionUtil.isPermissionGranted(context, it)
        }
    }

    /**
     * Check if permission Exists in Manifest
     *
     * @param context Application Context
     * @return Array<String> returns permission which are added in Manifest
     */
    private fun getRequiredPermission(context: Context): Array<String> {
        return REQUIRED_PERMISSIONS.filter {
            PermissionUtil.isPermissionInManifest(context, it)
        }.toTypedArray()
    }

    /**
     * Handle Requested Permission Result
     */
    fun onRequestPermissionsResult(requestCode: Int) {
        if (requestCode == PERMISSION_INTENT_REQ_CODE) {
            // Check again if permission is granted
            if (isPermissionGranted(this)) {
                // Permission is granted, Start Camera Intent
                startIntent(cameraActivityResult)
            } else {
                // Exit with error message
                val error = getString(R.string.permission_camera_denied)
                setError(error)
            }
        }
    }

    fun onActivityResult(resultCode: Int, data: Intent?) {
            if (resultCode == Activity.RESULT_OK) {
                handleResult()
            } else {
                setResultCancel()
            }
    }

    private fun handleResult() {
        activity.setImage(Uri.fromFile(mCameraFile))
    }

    /**
     * Delete Camera file is exists
     */
    override fun onFailure() {
        delete()
    }

    /**
     * Delete Camera File, If not required
     *
     * After Camera Image Crop/Compress Original File will not required
     */
    fun delete() {
        mCameraFile?.delete()
        mCameraFile = null
    }

}