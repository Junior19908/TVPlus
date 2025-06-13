package com.skysinc.tvplus

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.skysinc.tvplus.R
import com.skysinc.tvplus.ui.MainFragment

class HomeActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, MainFragment()) // usa o layout root
                .commit()
        }
    }
}
