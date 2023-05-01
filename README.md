# 📸Image Picker Library for Android


Easy to use and configurable library to **Pick an image from the Gallery or Capture image using Camera**. It also allows to **Crop and Compresses the Image based on Aspect Ratio, Resolution and Image Size**.

Almost 90% of the app that I have developed has an Image upload feature. Along with the image selection, Sometimes  I needed a crop feature for profile image for that I've used uCrop. Most of the time I need to compress the image as the image captured from the camera is more than 5-10 MBs and sometimes we have a requirement to upload images with specific resolution/size, in that case, image compress is the way to go option. To simplify the image pick/capture option I have created ImagePicker library. I hope it will be useful to all.

# 🐱‍🏍Features:
	
* Pick Gallery Image
* Pick Image from Google Drive
* Capture Camera Image
* Crop Image (Crop image based on provided aspect ratio or let user choose one)
* Compress Image (Compress image based on provided resolution and size)
* Retrieve Image Result as Uri object (Retrieve as File object feature is removed in v2.0 to support scope storage)
* Handle runtime permission for camera
* Does not require storage permission to pick gallery image or capture new image.

# 🎬Preview


   Profile Image Picker    |         Gallery Only      |       Camera Only        |
:-------------------------:|:-------------------------:|:-------------------------:
![](https://github.com/NareshDhakecha/NDImagePicker/blob/master/art/imagepicker_profile_demo.gif)  |  ![](https://github.com/NareshDhakecha/NDImagePicker/blob/master/art/imagepicker_gallery_demo.gif.gif)  |  ![](https://github.com/NareshDhakecha/NDImagePicker/blob/master/art/imagepicker_camera_demo.gif.gif)

# 💻Usage


1. Gradle dependency:

	```groovy
	allprojects {
	   repositories {
           	maven { url "https://jitpack.io" }
	   }
	}
	```

    ```groovy
   implementation 'ccom.github.NareshDhakecha:NDImagePicker:1.0.0'
    ```



2. The ImagePicker configuration is created using the builder pattern.

	**Kotlin**

	```kotlin
    ImagePicker.with(this)
            .crop()	    			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    ```

    **Java**

    ```kotlin
    ImagePicker.with(this)
            .crop()	    			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    ```

3. Handling results

    **Override `onActivityResult` method and handle ImagePicker result.**

    ```kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (resultCode == Activity.RESULT_OK) {
             //Image Uri will not be null for RESULT_OK
             val uri: Uri = data?.data!!

             // Use Uri object instead of File to avoid storage permissions
             imgProfile.setImageURI(fileUri)
         } else if (resultCode == ImagePicker.RESULT_ERROR) {
             Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
         } else {
             Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
         }
    }
    ```

    **Inline method (with registerForActivityResult, Only Works with FragmentActivity and AppCompatActivity)**

    i. Add required dependency for registerForActivityResult API

    ```groovy
	implementation "androidx.activity:activity-ktx:1.2.3"
    implementation "androidx.fragment:fragment-ktx:1.3.3"
    ```

    ii. Declare this method inside fragment or activity class

    ```kotlin
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val fileUri = data?.data!!

                mProfileUri = fileUri
                imgProfile.setImageURI(fileUri)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    ```

    iii. Create ImagePicker instance and launch intent

    ```kotlin
    ImagePicker.with(this)
            .compress(1024)         //Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    ```


# 🎨Customization

 *  Pick image using Gallery

	```kotlin
	ImagePicker.with(this)
		.galleryOnly()	//User can only select image from Gallery
		.start()	//Default Request Code is ImagePicker.REQUEST_CODE
    ```

 *  Capture image using Camera

	```kotlin
	ImagePicker.with(this)
		.cameraOnly()	//User can only capture image using Camera
		.start()
    ```
 *  Crop image

    ```kotlin
    ImagePicker.with(this)
		.crop()	    //Crop image and let user choose aspect ratio.
		.start()
	```
 *  Crop image with fixed Aspect Ratio

    ```kotlin
    ImagePicker.with(this)
		.crop(16f, 9f)	//Crop image with 16:9 aspect ratio
		.start()
    ```
 *  Crop square image(e.g for profile)

     ```kotlin
     ImagePicker.with(this)
         .cropSquare()	//Crop square image, its same as crop(1f, 1f)
         .start()
    ```
 *  Compress image size(e.g image should be maximum 1 MB)

	```kotlin
    ImagePicker.with(this)
		.compress(1024)	//Final image size will be less than 1 MB
		.start()
    ```
 *  Set Resize image resolution

    ```kotlin
    ImagePicker.with(this)
		.maxResultSize(620, 620)	//Final image resolution will be less than 620 x 620
		.start()
    ```
 *  Intercept ImageProvider, Can be used for analytics

    ```kotlin
    ImagePicker.with(this)
        .setImageProviderInterceptor { imageProvider -> //Intercept ImageProvider
            Log.d("ImagePicker", "Selected ImageProvider: "+imageProvider.name)
        }
        .start()
    ```
 *  Intercept Dialog dismiss event

	```kotlin
    ImagePicker.with(this)
    	.setDismissListener {
    		// Handle dismiss event
    		Log.d("ImagePicker", "onDismiss");
    	}
    	.start()
    ```

 *  Specify Directory to store captured, cropped or compressed images. *Do not use external public storage directory (i.e. Environment.getExternalStorageDirectory())*

    ```kotlin
    ImagePicker.with(this)
       /// Provide directory path to save images, Added example saveDir method. You can choose directory as per your need.

       //  Path: /storage/sdcard0/Android/data/package/files
       .saveDir(getExternalFilesDir(null)!!)
       //  Path: /storage/sdcard0/Android/data/package/files/DCIM
       .saveDir(getExternalFilesDir(Environment.DIRECTORY_DCIM)!!)
       //  Path: /storage/sdcard0/Android/data/package/files/Download
       .saveDir(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!)
       //  Path: /storage/sdcard0/Android/data/package/files/Pictures
       .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)
       //  Path: /storage/sdcard0/Android/data/package/files/Pictures/ImagePicker
       .saveDir(File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!, "ImagePicker"))
       //  Path: /storage/sdcard0/Android/data/package/files/ImagePicker
       .saveDir(getExternalFilesDir("ImagePicker")!!)
       //  Path: /storage/sdcard0/Android/data/package/cache/ImagePicker
       .saveDir(File(getExternalCacheDir(), "ImagePicker"))
       //  Path: /data/data/package/cache/ImagePicker
       .saveDir(File(getCacheDir(), "ImagePicker"))
       //  Path: /data/data/package/files/ImagePicker
       .saveDir(File(getFilesDir(), "ImagePicker"))

      // Below saveDir path will not work, So do not use it
      //  Path: /storage/sdcard0/DCIM
      //  .saveDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
      //  Path: /storage/sdcard0/Pictures
      //  .saveDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
      //  Path: /storage/sdcard0/ImagePicker
      //  .saveDir(File(Environment.getExternalStorageDirectory(), "ImagePicker"))

        .start()
    ```

 *  Limit MIME types while choosing a gallery image

    ```kotlin
    ImagePicker.with(this)
        .galleryMimeTypes(  //Exclude gif images
            mimeTypes = arrayOf(
              "image/png",
              "image/jpg",
              "image/jpeg"
            )
          )
        .start()
    ```

 *  You can also specify the request code with ImagePicker

    ```kotlin
    ImagePicker.with(this)
		.maxResultSize(620, 620)
		.start(101)	//Here 101 is request code, you may use this in onActivityResult
    ```

 *  Add Following parameters in your **colors.xml** file, If you want to customize uCrop Activity.

    ```xml
    <resources>
        <!-- Here you can add color of your choice  -->
        <color name="ucrop_color_toolbar">@color/teal_500</color>
        <color name="ucrop_color_statusbar">@color/teal_700</color>
        <color name="ucrop_color_widget_active">@color/teal_500</color>
    </resources>
    ```

# 💥Compatibility

  * Library - Android Kitkat 4.4+ (API 19)
  * Sample - Android Kitkat 4.4+ (API 19)

## License

    Copyright 2019-2023, NARESH DHAKECHA

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
