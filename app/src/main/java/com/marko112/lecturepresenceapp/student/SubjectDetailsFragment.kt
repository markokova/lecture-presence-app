package com.marko112.lecturepresenceapp.student

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import com.marko112.lecturepresenceapp.CaptureActivityPortrait
import com.marko112.lecturepresenceapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.w3c.dom.Text


class SubjectDetailsFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var subjectId = ""
    private var studentId = ""
    private var lectureDuration = 2
    private var aeDuration = 2
    private var leDuration = 2
    private var ceDuration = 2

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_subject_details, container, false)

        val subjectName = view.findViewById<TextView>(R.id.qr_code_subject_name)
        val totalAENum = view.findViewById<TextView>(R.id.subject_details_AE_total_hours)
        val totalLectureNum = view.findViewById<TextView>(R.id.subject_details_Lt_total_hours)
        val totalLENum = view.findViewById<TextView>(R.id.subject_details_LE_total_hours)
        val totalCENum = view.findViewById<TextView>(R.id.subject_details_CE_total_hours)
        val attendedLectureNum = view.findViewById<TextView>(R.id.attended_lectures_number)
        val attendedAeNum = view.findViewById<TextView>(R.id.attended_ae_number)
        val attendedLeNum = view.findViewById<TextView>(R.id.attended_le_number)
        val attendedCeNum = view.findViewById<TextView>(R.id.attended_ce_number)
        val heldLectureNum = view.findViewById<TextView>(R.id.held_lecture_number)
        val heldAeNum = view.findViewById<TextView>(R.id.held_ae_number)
        val heldLeNum = view.findViewById<TextView>(R.id.held_le_number)
        val heldCeNum = view.findViewById<TextView>(R.id.held_ce_number)

        val goToScanScreenButton = view.findViewById<ImageButton>(R.id.goToScanScreenButton)

        subjectName.text = arguments?.getCharSequence("Name").toString()
        totalAENum.text = arguments?.getCharSequence("AETotalNumber").toString().toInt().toString()
        totalLectureNum.text = arguments?.getCharSequence("LectureTotalNumber").toString()
        totalLENum.text = arguments?.getCharSequence("LETotalNumber").toString()
        totalCENum.text = arguments?.getCharSequence("CETotalNumber").toString()

        lectureDuration = arguments?.getCharSequence("LectureDuration").toString().toInt()
        aeDuration = arguments?.getCharSequence("AEDuration").toString().toInt()
        leDuration = arguments?.getCharSequence("LEDuration").toString().toInt()
        ceDuration = arguments?.getCharSequence("CEDuration").toString().toInt()

        //progressBar declarations
        val attendedLectureProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_lecture_attended_student)
        val attendedAeProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_ae_attended_student)
        val attendedLeProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_le_attended_student)
        val attendedCeProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_ce_attended_student)
        val heldLecturesProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_lecture_held_student)
        val heldAeProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_ae_held_student)
        val heldLeProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_le_held_student)
        val heldCeProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_ce_held_student)


        subjectId = arguments?.getCharSequence("Id").toString()
        studentId = auth.currentUser?.uid.toString()

        CoroutineScope(Dispatchers.Main).launch {
            val result = getStudentAttendance()
            val resultLecture = result?.get("Lecture")?.toDouble(); val resultAe = result?.get("AE")?.toDouble()
            val resultLe = result?.get("LE")?.toDouble(); val resultCe = result?.get("CE")?.toDouble()

            attendedLectureNum.text = (resultLecture?.times(lectureDuration))?.toInt().toString()
            attendedAeNum.text = (resultAe?.times(aeDuration))?.toInt().toString()
            attendedLeNum.text = (resultLe?.times(leDuration))?.toInt().toString()
            attendedCeNum.text = (resultCe?.times(ceDuration))?.toInt().toString()

            val attendedLectures = (resultLecture?.div(totalLectureNum.text.toString().toDouble()))?.times(100.0)
            val attendedAe = (resultAe?.div(totalAENum.text.toString().toDouble())?.times(100.0))
            val attendedLe = (resultLe?.div(totalLENum.text.toString().toDouble())?.times(100.0))
            val attendedCe = (resultCe?.div(totalCENum.text.toString().toDouble())?.times(100.0))

            Log.d(TAG,"attendedLectures: $attendedLectures, result: $result, resultLecture: $resultLecture, text:${totalLectureNum.text}")

            val heldLectures = calculateHeldActivitiesAmount(lectureDuration, "Lectures")
            val heldAe = calculateHeldActivitiesAmount(aeDuration, "AuditoryExercises")
            val heldLe = calculateHeldActivitiesAmount(leDuration, "LaboratoryExercises")
            val heldCe = calculateHeldActivitiesAmount(ceDuration, "ConstructionExercises")

            heldLectureNum.text = heldLectures.toString()
            heldAeNum.text = heldAe.toString()
            heldLeNum.text = heldLe.toString()
            heldCeNum.text = heldCe.toString()

            attendedLectureProgressBar.progress = attendedLectures?.times(lectureDuration)?.toInt() ?: 0
            attendedAeProgressBar.progress = attendedAe?.times(aeDuration)?.toInt() ?: 0
            attendedLeProgressBar.progress = attendedLe?.times(leDuration)?.toInt() ?: 0
            attendedCeProgressBar.progress = attendedCe?.times(ceDuration)?.toInt() ?: 0

            heldLecturesProgressBar.progress = ((heldLectures.toDouble()/totalLectureNum.text.toString().toDouble())*100).toInt()
            heldAeProgressBar.progress = ((heldAe.toDouble()/totalAENum.text.toString().toDouble())*100).toInt()
            heldLeProgressBar.progress = ((heldLe.toDouble()/totalLENum.text.toString().toDouble())*100).toInt()
            heldCeProgressBar.progress = ((heldCe.toDouble()/totalCENum.text.toString().toDouble())*100).toInt()
        }

        val scanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            val resultCode = result.resultCode
            val data = result.data

            if(resultCode == RESULT_OK && data != null){
                val scanResult = IntentIntegrator.parseActivityResult(resultCode, data)

                if(scanResult != null && scanResult.contents != null){
                    val qrCodeString = scanResult.contents

                    CoroutineScope(Dispatchers.Main).launch {
                        addStudentsAttendance(qrCodeString)
                        addStudentToActivity(qrCodeString)
                    }
                }else{
                    Toast.makeText(this.context,"Neuspješno skeniranje.",Toast.LENGTH_SHORT).show()
                }
            }
        }

        goToScanScreenButton.setOnClickListener {
            val integrator = IntentIntegrator(this.activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Skeniraj QR kod")
            integrator.setCameraId(0) // Use the rear camera
            integrator.captureActivity = CaptureActivityPortrait::class.java
            integrator.setOrientationLocked(true)

            val scanIntent = integrator.createScanIntent()
            scanLauncher.launch(scanIntent)
        }
        return view
    }

    private suspend fun calculateHeldActivitiesAmount(
        activityDuration: Int,
        activityType: String
    ): Int = coroutineScope{
        try {
            val querySnapshot = db.collection("Subjects")
                .document(subjectId)
                .collection(activityType)
                .get()
                .await()
            val result = querySnapshot.size() * activityDuration
            //number of held activities * duration of one activity
            return@coroutineScope result
        }catch(e: Exception){
            Log.d(ContentValues.TAG, "error calculating held activities amount, $e")
            return@coroutineScope -1
        }
    }

    private suspend fun getStudentAttendance(): HashMap<String, Long>? = coroutineScope{
        try {
            var subjectAttendance: HashMap<String, Long>? = null
            Log.d(TAG,"studentId: $studentId, subjectId:$subjectId")
            val documentSnapshot = db.collection("Students")
                .document(studentId)
                .get()
                .await()

            val attendanceData = documentSnapshot.get("Attendance") as? HashMap<String, Map<String, Long>>
            subjectAttendance = attendanceData?.get(subjectId) as HashMap<String, Long>?

            return@coroutineScope subjectAttendance
        }catch(e: Exception){
            return@coroutineScope null
        }
    }

    private suspend fun addStudentsAttendance(qrCodeString: String){
        val activityType = qrCodeString.substringAfter("/").dropLast(1)
        val activityDuration = qrCodeString.last().toString().toLong()
        val activityTypeShort = getActivityTypeShort(activityType)

        //TODO - smislit kako update-at progress bar i podatke na subjectdetails fragmentu kada se doda prisutnost
        //TODO - nekako spriječit da student moze vise put skenirat qr od jednog predavanja
        auth.currentUser?.let { student ->
            db.collection("Students")
                .document(student.uid)
                .update("Attendance.$subjectId.$activityTypeShort", FieldValue.increment(activityDuration))
                .await()
        }
    }

    private suspend fun addStudentToActivity(qrCodeString: String){
        val activityId = qrCodeString.substringBefore("/")
        val activityType = qrCodeString.substringAfter("/").dropLast(1)

        val studentId = auth.currentUser?.uid
        db.collection("Subjects")
            .document(subjectId)
            .collection(activityType)
            .document(activityId)
            .update("StudentIds",FieldValue.arrayUnion(studentId))
            .await()
    }

    private fun getActivityTypeShort(activityType: String): String {
        if(activityType == "Lectures"){
            return "Lecture"
        }else if(activityType == "AuditoryExercises"){
            return "AE"
        }else if(activityType == "LaboratoryExercises"){
            return "LE"
        }else if(activityType == "ConstructionExercises"){
            return "CE"
        }else{
            return ""
        }
    }


}