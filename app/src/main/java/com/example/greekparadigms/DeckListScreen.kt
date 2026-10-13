package com.example.greekparadigms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greekparadigms.ui.theme.GreekParadigmsTheme

@Composable
fun DeckListScreen( //shows a list of decks
    decks: List<Deck>,
    onPick: (Deck) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) { //padding around the content
        Text( //shows choose a deck text
            text = "Choose a deck",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        decks.forEach { deck -> //for each deck, show a button with the name and number of forms
            Button(
                onClick = { onPick(deck) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("${deck.name}  (${deck.forms.size})")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeckListScreenPreview() {
    GreekParadigmsTheme {
        DeckListScreen(decks = decks, onPick = {})
    }
}