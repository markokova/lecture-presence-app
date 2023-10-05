package com.marko112.lecturepresenceapp.professor

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
//import android.os.Build.VERSION_CODES.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.marko112.lecturepresenceapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SubjectDetailsProfessorFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var subjectId: String = ""
    private var subjectName: String = ""
    private var activityToCreate = ""
    private var activityName: String = ""
    private val bundle = Bundle()

    private var lectureDuration = 4
    private var aEDuration = 4
    private var lEDuration = 4
    private var cEDuration = 4

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_subject_details_professor, container, false)

        subjectId = arguments?.getCharSequence("Id").toString()
        subjectName = arguments?.getCharSequence("Name").toString()

        val subjectNameTextView = view.findViewById<TextView>(R.id.qr_code_subject_name)
        val totalAENum = view.findViewById<TextView>(R.id.subject_details_AE_total_hours)
        val totalLectureNum = view.findViewById<TextView>(R.id.subject_details_Lt_total_hours)
        val totalLENum = view.findViewById<TextView>(R.id.subject_details_LE_total_hours2)
        val totalCENum = view.findViewById<TextView>(R.id.subject_details_CE_total_hours2)
        val heldLectures = view.findViewById<TextView>(R.id.prof_subject_held_Lt_ammount)
        val heldAE = view.findViewById<TextView>(R.id.prof_subject_held_AE_amount)
        val heldLE = view.findViewById<TextView>(R.id.prof_subject_held_LE_ammount)
        val heldCE = view.findViewById<TextView>(R.id.prof_subject_held_CE_ammount)

        val activityChoiceGroup = view.findViewById<RadioGroup>(R.id.activity_choice_radio_group)
        lectureDuration = arguments?.getCharSequence("LectureDuration").toString().toInt()
        aEDuration = arguments?.getCharSequence("AEDuration").toString().toInt()
        lEDuration = arguments?.getCharSequence("LEDuration").toString().toInt()
        cEDuration = arguments?.getCharSequence("CEDuration").toString().toInt()

        CoroutineScope(Dispatchers.Main).launch {
            val lectureResult = calculateHeldActivitiesAmount(lectureDuration, "Lectures")
            if(lectureResult != -1){
                heldLectures.text = lectureResult.toString()
                val lectureProgressBar = view.findViewById<ProgressBar>(R.id.professor_lecture_progress_bar)
                lectureProgressBar.progress = ((heldLectures.text.toString().toDouble()/
                totalLectureNum.text.toString().toDouble())*100).toInt()
            }else{
                heldLectures.text = "-1"
            }
            val aeResult = calculateHeldActivitiesAmount(aEDuration, "AuditoryExercises")
            if(aeResult != -1){
                heldAE.text = aeResult.toString()
                val aeProgressBar = view.findViewById<ProgressBar>(R.id.professor_ae_progress_bar)
                aeProgressBar.progress = ((heldAE.text.toString().toDouble()/
                totalAENum.text.toString().toDouble())*100).toInt()
            }else{
                heldAE.text = "-1"
            }
            val leResult = calculateHeldActivitiesAmount(lEDuration, "LaboratoryExercises")
            if(leResult != -1){
                heldLE.text = leResult.toString()
                val leProgressBar = view.findViewById<ProgressBar>(R.id.professor_LE_progress_bar)
                leProgressBar.progress = ((heldLE.text.toString().toDouble()/
                        totalLENum.text.toString().toDouble())*100).toInt()
            }else{
                heldLE.text = "-1"
            }
            val ceResult = calculateHeldActivitiesAmount(cEDuration, "ConstructionExercises")
            if(ceResult != -1){
                heldCE.text = ceResult.toString()
                val ceProgressBar = view.findViewById<ProgressBar>(R.id.professor_CE_progress_bar)
                ceProgressBar.progress = ((heldCE.text.toString().toDouble()/
                        totalCENum.text.toString().toDouble())*100).toInt()
            }else{
                heldCE.text = "-1"
            }
        }

        activityChoiceGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.lecture_radio_button ->{
                    activityToCreate = "Lectures"
                    activityName = "Predavanje"
                }
                R.id.AE_radio_button ->{
                    activityToCreate = "AuditoryExercises"
                    activityName = "AV"
                }
                R.id.LE_radio_button ->{
                    activityToCreate = "LaboratoryExercises"
                    activityName = "LV"
                }
                R.id.CE_radio_button ->{
                    activityToCreate = "ConstructionExercises"
                    activityName = "KV"
                }
            }
        }

        val addActivityButton = view.findViewById<ImageButton>(R.id.add_activity_button)
        addActivityButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = addActivity()
                if(result){
                    Toast.makeText(context, "Uspješno dodana aktivnost!", Toast.LENGTH_LONG).show()
                } else{
                    Toast.makeText(context, "Odaberite vrstu aktivnosti koju želite kreirati.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val historyButton = view.findViewById<Button>(R.id.prof_history_button)
        historyButton.setOnClickListener {
            if(activityToCreate != ""){
                goToHistoryFragment()
            }else{
                Toast.makeText(this.context,"Odaberite vrstu aktivnosti kako bi vidjeli povijest.",Toast.LENGTH_SHORT).show()
            }
        }

        subjectNameTextView.text = subjectName
        totalAENum.text = arguments?.getCharSequence("AETotalNumber").toString().toInt().toString()
        totalLectureNum.text = arguments?.getCharSequence("LectureTotalNumber").toString()
        totalLENum.text = arguments?.getCharSequence("LETotalNumber").toString()
        totalCENum.text = arguments?.getCharSequence("CETotalNumber").toString()


        return view
    }

    private suspend fun addActivity() = coroutineScope {
        try {
            val data = createNewActivity(activityToCreate)
            //npr svi kolegiji korisnika => matematika 1(izabran kolegij) => predavanja(kliknuta aktivnost) => kreiraj preadvanje
            db.collection("Subjects")
                .document(subjectId)
                .collection(activityToCreate)
                .add(data)
                .addOnSuccessListener { documentReference ->
                    goToQrCodeFragment(documentReference.id)
                }
            //TODO - potrebno je pozvat funkciju calculateHeldActivitiesAmmount da kad se doda aktivnost da se refreshaju sve vrijednosti koje se prikazuju - mozda cak i ne moram
            //TODO - jer ce me morat odvest na novi fragment kada se kreira aktivnost
            return@coroutineScope true
        } catch (e: Exception){
            Log.d(TAG,"error adding new activity, $e")
            return@coroutineScope false
        }
    }

    private suspend fun createNewActivity(
        activityToCreate: String
    ): HashMap<String, Serializable?> {

        val querySnapshot = db.collection("Subjects")
            .document(subjectId)
            .collection(activityToCreate)
            .get()
            .await()
        val numberOfActivities = querySnapshot.size()

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
        val currentTime = LocalDateTime.now().format(formatter)

        bundle.putString("activityNumber",(numberOfActivities + 1).toString())
        bundle.putString("activityDate",currentTime)

        return hashMapOf(
            "Date" to currentTime,
            "StudentIds" to ArrayList<String>(),
            "ProfessorId" to auth.currentUser?.uid,
            "ActivityNumber" to (numberOfActivities + 1).toLong(),
        )
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
            Log.d(TAG, "error calculating held activities amount, $e")
            return@coroutineScope -1
        }
    }

    private fun goToQrCodeFragment(LessonId: String) {
        val qrCodeFragment = QRCodeFragment()

        bundle.putString("lessonId",LessonId)
        bundle.putString("subjectName", subjectName)
        bundle.putString("activityName", activityName)
        bundle.putString("LectureDuration", lectureDuration.toString())
        bundle.putString("LEDuration", lEDuration.toString())
        bundle.putString("AEDuration", aEDuration.toString())
        bundle.putString("CEDuration", cEDuration.toString())


        qrCodeFragment.arguments = bundle

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, qrCodeFragment).addToBackStack(null).commit()
    }

    private fun goToHistoryFragment(){
        val lectureHistoryFragment = LectureHistoryFragment()
        val fragmentTransaction : FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()

        bundle.putString("subjectId", arguments?.getCharSequence("Id").toString())
        bundle.putString("activityType", activityToCreate)
        bundle.putString("subjectName", subjectName)
        bundle.putString("activityName", activityName)
        lectureHistoryFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragmentContainerView, lectureHistoryFragment).addToBackStack(null).commit()
    }

}