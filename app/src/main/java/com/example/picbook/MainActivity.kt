package com.example.picbook

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity: AppCompatActivity(){

    private var detailsFragLarge: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        detailsFragLarge = supportFragmentManager.findFragmentById(R.id.details_frag_large)
    }

    /**
     * If target device is a tablet, only one activity is displayed.
     * So tab bar will contain the menu delete button which call relative DetailFragment function.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(detailsFragLarge != null){
            menuInflater.inflate(R.menu.menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete){

            (detailsFragLarge as DetailFragment).deleteImage()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}