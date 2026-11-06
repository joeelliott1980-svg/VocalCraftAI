package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.SongRepository
import com.example.ui.MainScreen
import com.example.ui.theme.VocalCraftTheme

class MainActivity : ComponentActivity() {

    private lateinit var songRepository: SongRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        songRepository = SongRepository(applicationContext)

        setContent {
            VocalCraftTheme {
                MainScreen(repository = songRepository)
            }
        }
    }
}
