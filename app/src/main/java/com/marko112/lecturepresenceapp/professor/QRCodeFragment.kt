package com.marko112.lecturepresenceapp.professor

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.marko112.lecturepresenceapp.*


class QRCodeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    var lectureDuration = 4
    var aeDuration = 2
    var leDuration = 4
    var ceDuration = 4

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_qr_code, container, false)

        val subjectName = arguments?.getCharSequence("subjectName").toString()
        val activityName = arguments?.getCharSequence("activityName").toString()
        lectureDuration = arguments?.getCharSequence("LectureDuration").toString().toInt()
        leDuration = arguments?.getCharSequence("LEDuration").toString().toInt()
        aeDuration = arguments?.getCharSequence("AEDuration").toString().toInt()
        ceDuration = arguments?.getCharSequence("CEDuration").toString().toInt()


        val fragmentTitle = view.findViewById<TextView>(R.id.qr_code_subject_name)
        fragmentTitle.text = subjectName
            .plus("-")
            .plus(activityName)

        val detailsButton = view.findViewById<Button>(R.id.qr_code_details_button)
        detailsButton.setOnClickListener {
            val lectureDetailsFragment = LectureDetailsFragment()
            val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            val bundle = Bundle()
            bundle.putString("subjectName", subjectName)
            bundle.putString("activityName", activityName)
            bundle.putString("activityNumber", arguments?.getCharSequence("activityNumber").toString())
            bundle.putString("activityDate", arguments?.getCharSequence("activityDate").toString())
            bundle.putString("professorId", auth.currentUser?.uid)
            bundle.putStringArrayList("studentIds", ArrayList<String>())

            lectureDetailsFragment.arguments = bundle

            fragmentTransaction.replace(R.id.fragmentContainerView,lectureDetailsFragment).addToBackStack(null).commit()
        }

        val lessonId = arguments?.getCharSequence("lessonId").toString()
        val lessonInfo = getActivityInfo(activityName)
        //this string will be used for qr code creation
        val qrCodeString = "$lessonId/${lessonInfo?.activityType}${lessonInfo?.activityDuration}"

        val bitmap = generateQRCode(qrCodeString)

        val qrCodeImageView = view.findViewById<ImageView>(R.id.qrCodeImageView)
        if(bitmap != null){
            qrCodeImageView.setImageBitmap(bitmap)
//            val emailIntent = Intent(Intent.ACTION_SEND)
//            emailIntent.putExtra(Intent.EXTRA_EMAIL,"marko.kovacevic1112@gmail.com")
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"QR kod za kreiranu aktivnost")
//            emailIntent.putExtra(Intent.EXTRA_TEXT,"Vaš QR kod:")
        }
        else{
            Log.d(TAG,"bitmap for qr code is null!")
        }

        return view
    }

    private fun generateQRCode(qrCodeString: String): Bitmap? {
        val writer = MultiFormatWriter()
        try {
            val matrix: BitMatrix = writer.encode(qrCodeString, BarcodeFormat.QR_CODE, 600, 600)
            val encoder = BarcodeEncoder()

            return encoder.createBitmap(matrix)

        } catch (e: WriterException) {
            Log.d(TAG, "$e, Pojavila se greška prilikom generiranja QR koda")
            return null
        }
    }

    private fun getActivityInfo(activityName: String): IActivity? {
        if(activityName == "Predavanje"){
            return LectureExercise("Lectures", lectureDuration)
        }else if(activityName == "AV"){
            return AuditoryExercise("AuditoryExercises", aeDuration)
        }else if(activityName == "LV"){
            return LaboratoryExercise("LaboratoryExercises", leDuration)
        }else if(activityName == "KV"){
            return ConstructionExercise("ConstructionExercises", ceDuration)
        }else{
            return null
        }
    }

}