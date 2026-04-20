package com.slumber.mobilehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.slumber.mobilehub.ui.app.SlumberMobileHubApp
import com.slumber.mobilehub.ui.theme.SlumberMobileHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SlumberMobileHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SlumberMobileHubApp()
                }
            }
        }
    }
}
