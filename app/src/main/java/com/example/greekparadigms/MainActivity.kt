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
    override fun onCreate(savedInstanceState: Bundle?) { //called when the creen starts
        super.onCreate(savedInstanceState) //passes the saved state the parent
        enableEdgeToEdge()
        setContent { //sets the content of the screen
            GreekParadigmsTheme {
                var selected by remember { mutableStateOf<Deck?>(null) } //holds the current deck
                var showingPractice by remember { mutableStateOf(true) } //holds whether to show practice or reference

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding -> //padding around the content
                    Column(modifier = Modifier.padding(innerPadding)) { //passes its children to the content

                        val current = selected //holds the current deck

                        if (current == null) { //if no deck is selected, show the deck list
                            DeckListScreen(
                                decks = decks,
                                onPick = { //sets selected to the current deck and shows practice
                                    selected = it
                                    showingPractice = true
                                }
                            )
                        } else { //if a deck is selected, show the forms
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(onClick = { selected = null }) { //sets selected to null and shows reference
                                    Text("Decks")
                                }
                                if (showingPractice) { //if showing practice, show practise button, otherwise show reference button
                                    OutlinedButton(onClick = { showingPractice = false }) {
                                        Text("Reference")
                                    }
                                } else { //if showing reference, show reference button, otherwise show practise button
                                    Button(onClick = { showingPractice = true }) {
                                        Text("Practise")
                                    }
                                }
                            }

                            if (showingPractice) { //if showing practice, show the practice screen, otherwise show the form list
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