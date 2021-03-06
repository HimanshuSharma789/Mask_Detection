package com.example.maskdetection

import android.graphics.Rect
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.frame.Frame


class MainActivity : AppCompatActivity() {

    lateinit var cameraOverlay: OverlayView
    lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraView = findViewById<CameraView>(R.id.cameraView)
        cameraOverlay = findViewById(R.id.cameraOverlay)
        cameraView.setLifecycleOwner(this)

        cameraView.frameProcessingExecutors = 3
        cameraView.frameProcessingPoolSize = 4


        val localModel = LocalModel.Builder()
            .setAssetFilePath("classification_model.tflite")
            .build()

//         Live detection and tracking
        val options =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .enableMultipleObjects()
                .setClassificationConfidenceThreshold(0.7f)
                .setMaxPerObjectLabelCount(1)
                .build()
        val objectDetector = ObjectDetection.getClient(options)


//        val options1 = CustomImageLabelerOptions.Builder(localModel)
//            .setConfidenceThreshold(0.5f)
//            .setMaxResultCount(5)
//            .build()
//        val labeler = ImageLabeling.getClient(options1)


        cameraView.addFrameProcessor { frame ->
//            extractDataFromFrame(it, localModel)
            objectDetector.process(getVisionImageFromFrame(frame))
                .addOnSuccessListener {
                    val list = mutableListOf<Pair<Rect, String>>()

                    it.forEach { item ->
                        val boundingBox = item.boundingBox
                        for (label in item.labels) {
                            val text = label.text

                            if (label.confidence > 0.75) {

                                list.add(Pair(boundingBox, text))
//                                textView.text = (text + " " + String.format("%.2f%%",confidence*100))
                            } else {
//                                textView.text = ""
                                cameraOverlay.setTargets(listOf(Pair(Rect(), "")))
                            }
                        }
                    }
                    cameraOverlay.setTargets(list)
//                    callback("object detected")
                }
                .addOnFailureListener {
//                callback("Unable to detect an object")
                    Log.v("TAG", it.toString())
                }
        }
    }


    private fun getVisionImageFromFrame(frame: Frame): InputImage {
        //ByteArray for the captured frame
        return if (frame.dataClass === ByteArray::class.java) {
            val data: ByteArray = frame.getData()
            InputImage.fromByteArray(
                data,
                frame.size.width,
                frame.size.height,
                frame.rotationToUser,
                InputImage.IMAGE_FORMAT_NV21
            )

        }
        // Process android.media.Image...
        // else if (frame.dataClass === Image::class.java) {
        else {
            val data: Image = frame.getData()
            InputImage.fromMediaImage(data, frame.rotationToUser)

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.camera_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_change_facing) {
            if(cameraView.facing == Facing.BACK) {
                cameraView.facing = Facing.FRONT
            } else {
                cameraView.facing = Facing.BACK
            }
        }
        return super.onOptionsItemSelected(item)
    }
}


