package com.androideditors.qrstudio

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF3647A0),
                    secondary = Color(0xFF5C69B7),
                    background = Color(0xFFF6F7FB),
                    surface = Color.White
                )
            ) {
                QrCodeScreen()
            }
        }
    }
}

@Composable
private fun QrCodeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var input by rememberSaveable { mutableStateOf("") }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var isWorking by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF6F7FB),
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "QR Studio",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B244E)
            )
            Text(
                text = "Create a QR code from any text or website link.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5F6681)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("1. Enter your content", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = input,
                        onValueChange = { value ->
                            input = value
                            // Never let an older QR be saved for freshly edited content.
                            generatedBitmap = null
                        },
                        label = { Text("Text or URL") },
                        placeholder = { Text("https://example.com") },
                        minLines = 3,
                        maxLines = 6,
                        enabled = !isGenerating && !isWorking,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val content = input
                            scope.launch {
                                isGenerating = true
                                try {
                                    generatedBitmap = withContext(Dispatchers.Default) {
                                        QrCodeGenerator.generate(content)
                                    }
                                } catch (e: Exception) {
                                    snackbar.showSnackbar(
                                        "Could not generate QR code. Try shorter text."
                                    )
                                } finally {
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = input.isNotBlank() && !isGenerating && !isWorking,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Generate QR Code")
                        }
                    }
                }
            }

            generatedBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "2. Your QR code",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated QR code",
                            modifier = Modifier
                                .size(260.dp)
                                .background(Color.White)
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            "Ready to scan, save or share",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5F6681)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isWorking = true
                                        try {
                                            withContext(Dispatchers.IO) {
                                                QrImageStorage.saveToGallery(context, bitmap)
                                            }
                                            snackbar.showSnackbar("Saved to Pictures/QR Studio")
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar("Unable to save QR image.")
                                        } finally {
                                            isWorking = false
                                        }
                                    }
                                },
                                enabled = !isWorking,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Save PNG") }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isWorking = true
                                        try {
                                            val uri = withContext(Dispatchers.IO) {
                                                QrImageStorage.createShareUri(context, bitmap)
                                            }
                                            QrImageStorage.share(context, uri)
                                        } catch (e: Exception) {
                                            snackbar.showSnackbar("Unable to share QR image.")
                                        } finally {
                                            isWorking = false
                                        }
                                    }
                                },
                                enabled = !isWorking,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF3647A0)
                                )
                            ) { Text("Share") }
                        }
                    }
                }
            }
            Text(
                "Tip: For website links, include https:// so scanners open the correct page.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5F6681)
            )
        }
    }
}
