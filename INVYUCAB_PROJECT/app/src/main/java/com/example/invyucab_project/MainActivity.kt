package com.example.invyucab_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.invyucab_project.core.navigations.NavGraph
import com.example.invyucab_project.ui.theme.INVYUCAB_PROJECTTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            INVYUCAB_PROJECTTheme {
               NavGraph()
            }
        }
    }
}

