package io.constructor.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.constructor.core.ConstructorIo
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ConstructorIo.appMovedToForeground()
        button.setOnClickListener { startActivity(Intent(this, SampleActivity::class.java)) }
        button2.setOnClickListener { startActivity(Intent(this, SampleActivityCustom::class.java)) }
        button3.setOnClickListener { ConstructorIo.trackConversion("testId", "id", 11.0) }
        button4.setOnClickListener { ConstructorIo.trackSearchResultClick("testTerm", "testId", "1") }
        button5.setOnClickListener { ConstructorIo.trackSearchResultsLoaded("testTerm", Random().nextInt(99) + 1) }
    }
}
