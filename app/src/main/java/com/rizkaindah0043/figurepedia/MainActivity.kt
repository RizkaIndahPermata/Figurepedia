package com.rizkaindah0043.figurepedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rizkaindah0043.figurepedia.screen.MainScreen
import com.rizkaindah0043.figurepedia.ui.theme.FigurepediaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FigurepediaTheme {
                MainScreen()
            }
        }
    }
}