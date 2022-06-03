package com.example.picbook

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.picbook.fragments.DetailFragment

class DisplayActivity: AppCompatActivity() {

    /**
     * If target device is a tablet, detailFragment will be null
     */
    private var detailFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        detailFragment = supportFragmentManager.findFragmentById(R.id.detailFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete){
            (detailFragment as DetailFragment).deleteImage()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}