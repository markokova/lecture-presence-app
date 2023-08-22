package com.marko112.lecturepresenceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainEmptyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_empty)
    }
/*
* MainEmptyActivity bi trebao sluziti da se switcha izmedju Login activitya i main activity ovisno o tome je li token istekao
*  => na taj nacin ce biti bolje korisnicko iskustvo jer se korisnik ne bi svaki put trebao prijavljivati
* umjesto toga se provjeri ako je token i dalje aktivan onda se pali main activity a ako nije onda se pali login activity
* */
}