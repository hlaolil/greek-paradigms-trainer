package com.example.greekparadigms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greekparadigms.ui.theme.GreekParadigmsTheme

@Composable
fun StudyScreen( //shows a list of forms to study
    forms: List<Form>,
    modifier: Modifier = Modifier,
    onAnswer: (Form, Boolean) -> Unit = { _, _ -> }
) {
    var index by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }

    if (forms.isEmpty()) { //if there are no forms, show a message
        Text("No forms to study.", modifier = modifier.padding(16.dp))
        return
    }

    if (index >= forms.size) { //if all forms have been studied, show a message with the number of correct answers
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Done", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("$correctCount of ${forms.size} correct")
            Spacer(Modifier.height(32.dp))
            Button(onClick = {
                index = 0
                correctCount = 0
                revealed = false
            }) {
                Text("Start again")
            }
        }
        return
    }

    val current = forms[index] //get the current form

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(current.paradigm, style = MaterialTheme.typography.labelLarge)

        Spacer(Modifier.height(48.dp))

        Text(
            text = current.slot,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif
        )

        Spacer(Modifier.height(24.dp))

        if (revealed) {
            Text(
                text = current.form,
                style = MaterialTheme.typography.displaySmall,
                fontFamily = FontFamily.Serif
            )

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = {
                    onAnswer(current, false)
                    index++
                    revealed = false
                }) {
                    Text("Not known")
                }
                Button(onClick = {
                    onAnswer(current, true)
                    correctCount++
                    index++
                    revealed = false
                }) {
                    Text("Known")
                }
            }
        } else {
            Button(onClick = { revealed = true }) {
                Text("Show answer")
            }
        }

        Spacer(Modifier.height(48.dp))

        Text("$correctCount correct · ${index + 1} of ${forms.size}")
    }
}

@Preview(showBackground = true)
@Composable
fun StudyScreenPreview() {
    GreekParadigmsTheme {
        StudyScreen(logos)
    }
}