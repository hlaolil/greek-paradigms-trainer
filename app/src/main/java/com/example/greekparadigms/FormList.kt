package com.example.greekparadigms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greekparadigms.ui.theme.GreekParadigmsTheme

@Composable
fun FormList(forms: List<Form>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
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
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Serif
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