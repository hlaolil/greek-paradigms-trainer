package com.example.greekparadigms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.greekparadigms.ui.theme.GreekParadigmsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreekParadigmsTheme {
                var selected by remember { mutableStateOf<Deck?>(null) }
                var showingPractice by remember { mutableStateOf(true) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {

                        val current = selected

                        if (current == null) {
                            DeckListScreen(
                                decks = decks,
                                onPick = {
                                    selected = it
                                    showingPractice = true
                                }
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(onClick = { selected = null }) {
                                    Text("Decks")
                                }
                                if (showingPractice) {
                                    OutlinedButton(onClick = { showingPractice = false }) {
                                        Text("Reference")
                                    }
                                } else {
                                    Button(onClick = { showingPractice = true }) {
                                        Text("Practise")
                                    }
                                }
                            }

                            if (showingPractice) {
                                StudyScreen(forms = current.forms)
                            } else {
                                FormList(forms = current.forms)
                            }
                        }
                    }
                }
            }
        }
    }
}