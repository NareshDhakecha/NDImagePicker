package com.ndsoftwares.imagepicker.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ndsoftwares.imagepicker.sample.databinding.DialogImageviewerBinding

//import kotlinx.android.synthetic.main.dialog_imageviewer.*

/**
 * Dialog to View Image
 */
class ImageViewerDialog : DialogFragment() {

    private lateinit var bv: DialogImageviewerBinding

    companion object {

        private const val EXTRA_IMAGE_RESOURCE = "extra.image_resource"

        @JvmStatic
        fun newInstance(resource: Int) = ImageViewerDialog().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_IMAGE_RESOURCE, resource)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bv = DialogImageviewerBinding.inflate(inflater, container, false)
        return bv.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bv.codeImg.setImageResource(arguments?.getInt(EXTRA_IMAGE_RESOURCE, 0) ?: 0)
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        bv.codeImg.setImageResource(arguments?.getInt(EXTRA_IMAGE_RESOURCE, 0) ?: 0)
//    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.setLayout(width, height)
            it.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
