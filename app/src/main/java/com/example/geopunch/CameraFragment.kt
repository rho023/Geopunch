//package com.example.geopunch
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.camera.lifecycle.ProcessCameraProvider
//import com.google.common.util.concurrent.ListenableFuture
//
//
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [CameraFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class CameraFragment : Fragment() {
//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
//
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_camera, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//            bindPreview(cameraProvider)
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }
//
//    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
//        val preview = Preview.Builder().build().also {
//            it.setSurfaceProvider(viewFinder.surfaceProvider)
//        }
//
//        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//
//        cameraProvider.bindToLifecycle(this, cameraSelector, preview)
//    }
//
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment CameraFragment.
//         */
//
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            CameraFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
//}