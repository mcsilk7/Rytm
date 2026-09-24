package com.example.rytm

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.rytm.data.RytmDatabase
import com.example.rytm.data.RytmRepository
import com.example.rytm.ui.RytmApp
import com.example.rytm.ui.RytmTheme
import com.example.rytm.ui.RytmViewModel
import com.example.rytm.ui.RytmViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: RytmViewModel by viewModels {
        RytmViewModelFactory(
            RytmRepository(RytmDatabase.getInstance(applicationContext))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(247, 245, 237)
        window.navigationBarColor = Color.WHITE
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RytmTheme { RytmApp(state, viewModel) }
        }
    }
}