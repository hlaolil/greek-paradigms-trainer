package com.example.greekparadigms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greekparadigms.ui.theme.GreekParadigmsTheme

data class Form(
    val id: Int = 0,
    val paradigm: String,
    val slot: String,
    val form: String
)

val logos = listOf(
    Form(1,  "λόγος (2nd decl.)", "nominative singular", "λόγος"),
    Form(2,  "λόγος (2nd decl.)", "genitive singular",   "λόγου"),
    Form(3,  "λόγος (2nd decl.)", "dative singular",     "λόγῳ"),
    Form(4,  "λόγος (2nd decl.)", "accusative singular", "λόγον"),
    Form(5,  "λόγος (2nd decl.)", "vocative singular",   "λόγε"),
    Form(6,  "λόγος (2nd decl.)", "nominative plural",   "λόγοι"),
    Form(7,  "λόγος (2nd decl.)", "genitive plural",     "λόγων"),
    Form(8,  "λόγος (2nd decl.)", "dative plural",       "λόγοις"),
    Form(9,  "λόγος (2nd decl.)", "accusative plural",   "λόγους"),
    Form(10, "λόγος (2nd decl.)", "vocative plural",     "λόγοι"),
)

@Composable
fun FormList(forms: List<Form>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        forms.forEach { form ->
            FormRow(form)
        }
    }
}

@Composable
fun FormRow(item: Form) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.slot,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = item.paradigm,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = item.form,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Serif
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FormListPreview() {
    GreekParadigmsTheme {
        FormList(logos)
    }
}