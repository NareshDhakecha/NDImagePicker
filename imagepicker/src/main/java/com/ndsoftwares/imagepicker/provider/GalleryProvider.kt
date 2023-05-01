package com.ndsoftwares.imagepicker.provider

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import com.ndsoftwares.imagepicker.ImagePickerActivity
import com.ndsoftwares.imagepicker.NDImagePicker
import com.ndsoftwares.imagepicker.R
import com.ndsoftwares.imagepicker.util.IntentUtils

class GalleryProvider(activity: ImagePickerActivity) :
BaseProvider(activity){

    // Mime types restrictions for gallery. By default all mime types are valid
    private val mimeTypes: Array<String>

    init {
        val bundle = activity.intent.extras ?: Bundle()

        // Get MIME types
        mimeTypes = bundle.getStringArray(NDImagePicker.EXTRA_MIME_TYPES) ?: emptyArray()
    }

    // Start Gallery Capture Intent
    fun startIntent(galleryActivityResult: ActivityResultLauncher<Intent>) {
        startGalleryIntent(galleryActivityResult)
    }

    // Start Gallery Intent
    private fun startGalleryIntent(galleryActivityResult: ActivityResultLauncher<Intent>) {
        val galleryIntent = IntentUtils.getGalleryIntent(activity, mimeTypes)
//        activity.startActivityForResult(galleryIntent, GALLERY_INTENT_REQ_CODE)
        galleryActivityResult.launch(galleryIntent)
    }

    fun onActivityResult(resultCode: Int, data: Intent?){
        if (resultCode == Activity.RESULT_OK) {
            handleResult(data)
        } else {
            setResultCancel()
        }
    }

    /**
     * This method will be called when final result fot this provider is enabled.
     */
    private fun handleResult(data: Intent?) {
        val uri = data?.data
        if (uri != null) {
            takePersistableUriPermission(uri)
            activity.setImage(uri)
        } else {
            setError(R.string.error_failed_pick_gallery_image)
        }
    }

    /**
     * Take a persistable URI permission grant that has been offered. Once
     * taken, the permission grant will be remembered across device reboots.
     */
    private fun takePersistableUriPermission(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }


}