package org.izv.psp.kotlinurl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.izv.psp.kotlinurl.ui.theme.KotlinUrlTheme
import java.net.HttpURLConnection
import java.net.URI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinUrlTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BuscadorDeCodigo(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BuscadorDeCodigo(modifier: Modifier = Modifier) {
    var url by remember { mutableStateOf("https://example.com") }
    var codigoFuente by remember { mutableStateOf("El código aparecerá aquí...") }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        TextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    codigoFuente = "Cargando..."
                    codigoFuente = httpGet(url)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver Código")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = codigoFuente,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)
        )
    }
}

suspend fun httpGet(urlString: String): String = withContext(Dispatchers.IO) {
    try {
        val finalUrl = if (!urlString.startsWith("http")) "https://$urlString" else urlString
        val connection = URI.create(finalUrl).toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = 1000
        connection.readTimeout = 1000
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()
        return@withContext response
    } catch (e: Exception) {
        return@withContext "Error: ${e.message}"
    }
}