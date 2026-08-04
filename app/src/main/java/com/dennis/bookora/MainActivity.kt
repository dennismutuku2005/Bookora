package com.dennis.bookora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dennis.bookora.navigation.BookoraNavGraph
import com.dennis.bookora.ui.theme.BookoraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookoraApp()
        }
    }
}

@Composable
fun BookoraApp() {
    BookoraTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BookoraNavGraph()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookoraAppPreview() {
    BookoraTheme {
        BookoraApp()
    }
}
