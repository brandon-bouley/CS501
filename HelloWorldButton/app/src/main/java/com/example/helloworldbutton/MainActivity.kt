package com.example.helloworldbutton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.helloworldbutton.ui.theme.HelloWorldButtonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloWorldButtonTheme {
                Surface( // Surface container for organization and styling
                    // Content fills max size, default background color
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloWorldButtonApp()
                }
            }
        }
    }
}

@Composable
fun HelloWorldButtonApp() {
    var showText by remember { mutableStateOf(false) } //Mutable state for the button

    Column( //Make it pretty by displaying in a column
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showText = true }) { //Button that changes the state of showText
            Text("Click Me")
        }

        if (showText) { //If showText is true, display the text
            Text("Hello World!")
        }
    }
}