package com.ndsoftwares.imagepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.ndsoftwares.imagepicker.listener.IDismissListener
import com.ndsoftwares.imagepicker.listener.IResultListener
import com.ndsoftwares.imagepicker.util.DialogHelper
import java.io.File

class NDImagePicker {

    companion object {

        const val RESULT_ERROR = 64

        internal const val EXTRA_IMAGE_PROVIDER = "extra.image_provider"
        internal const val EXTRA_CAMERA_DEVICE = "extra.camera_device"

        internal const val EXTRA_IMAGE_MAX_SIZE = "extra.image_max_size"
        internal const val EXTRA_CROP = "extra.crop"
        internal const val EXTRA_CROP_X = "extra.crop_x"
        internal const val EXTRA_CROP_Y = "extra.crop_y"
        internal const val EXTRA_MAX_WIDTH = "extra.max_width"
        internal const val EXTRA_MAX_HEIGHT = "extra.max_height"
        internal const val EXTRA_SAVE_DIRECTORY = "extra.save_directory"

        internal const val EXTRA_ERROR = "extra.error"
        internal const val EXTRA_FILE_PATH = "extra.file_path"
        internal const val EXTRA_MIME_TYPES = "extra.mime_types"

        @JvmStatic
        fun with(activity: Activity): Builder = Builder(activity)

        @JvmStatic
        fun with(fragment: Fragment): Builder = Builder(fragment)

    }

    class Builder(private val activity: Activity){

        private var fragment: Fragment? = null
        // Image Provider
        private var imageProvider = ImageProvider.BOTH

        // Mime types restrictions for gallery. by default all mime types are valid
        private var mimeTypes: Array<String> = emptyArray()

        // Crop Parameters
        private var cropX: Float = 0f
        private var cropY: Float = 0f
        private var crop: Boolean = false

        //Resize Parameters
        private var maxWidth: Int = 0
        private var maxHeight: Int = 0

        // Max File Size
        private var maxSize: Long = 0

        private var imageProviderInterceptor: ((ImageProvider) -> Unit)? = null

        /**
         * Dialog dismiss event listener
         */
        private var dismissListener: IDismissListener? = null

        /**
         * File Directory
         *
         * Camera, Crop, Compress Image Will be store in this directory.
         *
         * If null, Image will be stored in "{fileDir}/Images"
         */
        private var saveDir: String? = null


        //Call this while picking image for fragment.
        constructor(fragment: Fragment) : this(fragment.requireActivity()){
            this.fragment = fragment
        }

        // Specify Image Provider (Camera, Gallery or Both)
        fun provider(imageProvider: ImageProvider): Builder {
            this.imageProvider = imageProvider
            return this
        }

        // Only Capture image using Camera
        fun cameraOnly(): Builder {
            this.imageProvider = ImageProvider.CAMERA
            return this
        }

        // Only Pick image from gallery.
        fun galleryOnly(): Builder {
            this.imageProvider = ImageProvider.GALLERY
            return this
        }

        /**
         * Restrict mime types during gallery fetching, for instance if you do not want GIF images,
         * you can use arrayOf("image/png","image/jpeg","image/jpg")
         * by default array is empty, which indicates no additional restrictions, just images
         * @param mimeTypes
         */
        fun galleryMimeTypes(mimeTypes: Array<String>): Builder {
            this.mimeTypes = mimeTypes
            return this
        }

        /**
         * Set an aspect ratio for crop bounds.
         * User won't see the menu with other ratios options.
         *
         * @param x aspect ratio X
         * @param y aspect ratio Y
         */
        fun crop(x: Float, y: Float): Builder {
            cropX = x
            cropY = y
            return crop()
        }

        // Crop an image and let user set the aspect ratio.
        fun crop(): Builder {
            this.crop = true
            return this
        }

        // Crop Square Image, Useful for Profile Image.
        fun cropSquare(): Builder {
            return crop(1f, 1f)
        }

        // Max Width and Height of final image
        fun maxResultSize(width: Int, height: Int): Builder {
            this.maxWidth = width
            this.maxHeight = height
            return this
        }

        /**
         * Compress Image so that max image size can be below specified size
         *
         * @param maxSize Size in KB
         */
        fun compress(maxSize: Int): Builder {
            this.maxSize = maxSize * 1024L
            return this
        }

        /**
         * Provide Directory to store Captured/Modified images
         *
         * @param path Folder Directory
         */
        fun saveDir(path: String): Builder {
            this.saveDir = path
            return this
        }
        /**
         * Provide Directory to store Captured/Modified images
         *
         * @param file Folder Directory
         */
        fun saveDir(file: File?): Builder {
            this.saveDir = file?.absolutePath
            return this
        }

        /**
         * Intercept Selected ImageProvider,  Useful for Analytics
         *
         * @param interceptor ImageProvider Interceptor
         */
        fun setImageProviderInterceptor(interceptor: (ImageProvider) -> Unit): Builder {
            this.imageProviderInterceptor = interceptor
            return this
        }

        // Sets the callback that will be called when the dialog is dismissed for any reason.
        fun setDismissListener(listener: IDismissListener): Builder {
            this.dismissListener = listener
            return this
        }

        // Sets the callback that will be called when the dialog is dismissed for any reason.
        fun setDismissListener(listener: (() -> Unit)): Builder {
            this.dismissListener = object : IDismissListener {
                override fun onDismiss() {
                    listener.invoke()
                }
            }
            return this
        }

        // Start Image Picker Activity
        fun start(resultCallback: ActivityResultLauncher<Intent>) {
            if (imageProvider == ImageProvider.BOTH){
                // Pick Image Provider if not specified
                showImageProviderDialog(resultCallback)
            }else{
                startImagePickerActivity(resultCallback)
            }
        }

        private fun startImagePickerActivity(resultCallback: ActivityResultLauncher<Intent>) {
            val intent = Intent(activity, ImagePickerActivity::class.java)
            intent.putExtras(getBundle())
            resultCallback.launch(intent)
        }

        private fun getBundle(): Bundle = Bundle().apply {
            putSerializable(EXTRA_IMAGE_PROVIDER, imageProvider)
            putStringArray(EXTRA_MIME_TYPES, mimeTypes)

            putBoolean(EXTRA_CROP, crop)
            putFloat(EXTRA_CROP_X, cropX)
            putFloat(EXTRA_CROP_Y, cropY)

            putInt(EXTRA_MAX_WIDTH, maxWidth)
            putInt(EXTRA_MAX_HEIGHT, maxHeight)

            putLong(EXTRA_IMAGE_MAX_SIZE, maxSize)

            putString(EXTRA_SAVE_DIRECTORY, saveDir)
        }

        private fun showImageProviderDialog(resultCallback: ActivityResultLauncher<Intent>) {
            DialogHelper.showChooseAppDialog(
                activity,
                object : IResultListener<ImageProvider> {
                    override fun onResult(t: ImageProvider?) {
                        t?.let {
                            imageProvider = it
                            imageProviderInterceptor?.invoke(imageProvider)
                            startImagePickerActivity(resultCallback)
                        }
                    }
                },
                dismissListener
            )
        }
    }

}