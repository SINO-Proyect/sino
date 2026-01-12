package com.app.sino.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.sino.R
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val scrollState = rememberScrollState()
    
    val name = "John Doe"
    val username = "@johndoe_sino"
    val email = "john.doe@example.com"
    val degree = "Software Engineering"
    val progress = 0.65f // 65%
    val qrData = "sino://user?email=$email&name=$name"

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(qrData) {
        qrBitmap = generateQrCode(qrData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp), // Reducido padding horizontal de 24 a 16
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // QR Code Section
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .size(180.dp) // Ligeramente más pequeño para optimizar espacio
                .clip(RoundedCornerShape(28.dp))
                .background(SinoWhite)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Profile QR Code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } ?: CircularProgressIndicator(color = Color.Black)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Basic Info
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = SinoWhite
        )
        Text(
            text = username,
            style = MaterialTheme.typography.bodyLarge,
            color = SinoWhite.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Career Progress Section (NUEVO)
        CareerProgressCard(degree = degree, progress = progress)

        Spacer(modifier = Modifier.height(24.dp))

        // Friends Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FriendActionCard(
                iconRes = R.drawable.users_fill,
                label = "Friends",
                value = "124",
                modifier = Modifier.weight(1f),
                onClick = { /* Navigate */ }
            )
            FriendActionCard(
                iconRes = R.drawable.user_plus,
                label = "Requests",
                value = "12",
                modifier = Modifier.weight(1f),
                onClick = { /* Navigate */ },
                isSubtle = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personal Info
        Text(
            text = "PERSONAL INFORMATION",
            style = MaterialTheme.typography.labelSmall,
            color = SinoWhite.copy(alpha = 0.3f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
            letterSpacing = 1.sp
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoCard(label = "Age", value = "21", modifier = Modifier.weight(1f))
                InfoCard(label = "Gender", value = "Male", modifier = Modifier.weight(1f))
            }
            InfoCard(label = "University", value = "Stanford University", modifier = Modifier.fillMaxWidth())
            InfoCard(label = "Country", value = "United States", modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2B1010),
                contentColor = Color(0xFFFF5252).copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.2f))
        ) {
            Text(text = "Sign Out", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CareerProgressCard(degree: String, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SinoBlack)
            .border(androidx.compose.foundation.BorderStroke(1.dp, SinoWhite.copy(alpha = 0.05f)), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CURRENT DEGREE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SinoWhite.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = degree,
                    style = MaterialTheme.typography.titleMedium,
                    color = SinoWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                color = SinoWhite,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Bar Estilizada (Arcoíris Profesional)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(SinoWhite.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF50C878), // Esmeralda
                                Color(0xFF4FC3F7), // Celeste
                                Color(0xFF9575CD), // Lavanda
                                Color(0xFFFF8A65)  // Coral
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun FriendActionCard(iconRes: Int, label: String, value: String, modifier: Modifier = Modifier, isSubtle: Boolean = false, onClick: () -> Unit) {
    val bgColor = if (isSubtle) Color.Transparent else SinoBlack
    val borderColor = if (isSubtle) SinoWhite.copy(alpha = 0.1f) else Color.Transparent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(androidx.compose.foundation.BorderStroke(1.dp, borderColor), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = SinoWhite, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SinoWhite)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SinoWhite.copy(alpha = 0.5f))
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(painter = painterResource(id = R.drawable.caret_right), contentDescription = null, tint = SinoWhite.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SinoBlack)
            .border(androidx.compose.foundation.BorderStroke(1.dp, SinoWhite.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SinoWhite.copy(alpha = 0.3f), letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = SinoWhite)
    }
}

fun generateQrCode(text: String): Bitmap? {
    return try {
        val size = 512
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}