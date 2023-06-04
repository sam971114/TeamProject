package com.example.myrunmain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myrunmain.databinding.ActivityMainBinding
import com.example.myrunmain.databinding.ActivityNavigationBinding
import com.example.myrunmain.MainWindowFragment
import com.google.android.material.navigation.NavigationView

class NavigationActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    lateinit var binding : ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent = getIntent()
        var number = intent.getIntExtra("number", 0)
        when(number) {
            1 -> {
                val intent_main = Intent(this@NavigationActivity, MainActivity::class.java)
                startActivity(intent_main)
            }

            2 -> {
                val fragment = CourseRecomFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
                //.addToBackStack(null).commit()
            }

            3 -> {
                val fragment = NearbyCourseFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
            }
        }

        setSupportActionBar(binding.toolbar)


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navigationView.setNavigationItemSelectedListener { menuItem -> when (menuItem.itemId) {
            R.id.main_win -> {
                val intent_main = Intent(this@NavigationActivity, MainActivity::class.java)
                startActivity(intent_main)
            }
            R.id.nav_recom -> {
                val fragment = CourseRecomFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
            }
            R.id.nav_nearby -> {
                val fragment = NearbyCourseFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
            }
            R.id.nav_record -> {
                val intent_record = Intent(this@NavigationActivity, SecondActivity::class.java)
                startActivity(intent_record)
            }
            R.id.nav_weather -> {
                val intent_weather = Intent(this@NavigationActivity, WeatherActivity::class.java)
                startActivity(intent_weather)
            }
            R.id.nav_challenge -> {
                val intent_challenge = Intent(this@NavigationActivity, ChallegeActivity::class.java)
                startActivity(intent_challenge)
            }
            R.id.nav_setting -> {
                val intent_setting = Intent(this@NavigationActivity, SettingActivity::class.java)
                startActivity(intent_setting)
            }
        }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}