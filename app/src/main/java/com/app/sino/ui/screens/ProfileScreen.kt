package com.app.sino.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val userState by viewModel.userState.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    
    val name = userState?.fullName
    val username = userState?.username?.let { "@$it" }
    val email = userState?.email ?: "Loading..."
    val degree = userState?.degreeName
    val progress = userState?.progress?.toFloat() ?: 0f
    val planType = userState?.type?.uppercase() ?: "FREE"

    val joinedDate = userState?.dateRegister?.let { dateStr ->
        try {
            val parts = dateStr.substringBefore(" ").split("-")
            if (parts.size == 3) {
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else dateStr
        } catch (e: Exception) {
            dateStr
        }
    } ?: "N/A"

    val qrData = "sino://user?username=${userState?.username ?: "unknown"}"

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(qrData) {
        qrBitmap = generateQrCode(qrData)
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadUserProfile() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = SinoWhite.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, SinoWhite.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "$planType PLAN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SinoWhite,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp) // Enlarged container
                        .clip(RoundedCornerShape(32.dp))
                        .background(SinoWhite)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Profile QR Code",
                            modifier = Modifier.fillMaxSize().padding(2.dp),
                            contentScale = ContentScale.Fit
                        )
                    } ?: CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = name ?: "Guest User",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SinoWhite
            )
            if (username != null) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SinoWhite.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (degree != null) {
                CareerProgressCard(degree = degree, progress = progress)
            } else {
                 NoPlanCard()
            }

            Spacer(modifier = Modifier.height(24.dp))

            val followers = userState?.followersCount ?: 0
            val following = userState?.followingCount ?: 0
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = followers.toString(),
                    label = "Followers",
                    iconRes = R.drawable.users_fill,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = following.toString(),
                    label = "Following",
                    iconRes = R.drawable.users,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SinoWhite.copy(alpha = 0.05f))
                    .border(1.dp, SinoWhite.copy(alpha = 0.1f), RoundedCornerShape(16.dp)) // Subtle border
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileDetailItem(iconRes = R.drawable.ic_profile_filled, label = "Full Name", value = name ?: "Not set")
                ProfileDetailItem(iconRes = R.drawable.ic_profile_outline, label = "Email", value = email)
                ProfileDetailItem(iconRes = R.drawable.ic_calendar_filled, label = "Joined", value = joinedDate)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B1010),
                    contentColor = Color(0xFFFF5252).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.2f))
            ) {
                Text(text = "Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(value: String, label: String, iconRes: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SinoWhite.copy(alpha = 0.05f))
            .border(1.dp, SinoWhite.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = SinoWhite,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SinoWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SinoWhite.copy(alpha = 0.5f)
        )
    }
}



@Composable
fun NoPlanCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C2215)) // Dark orange/brown tint
            .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFFF9800),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "No Study Plan Active",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFE0B2)
            )
            Text(
                text = "Select a career to start tracking your progress.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFE0B2).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CareerProgressCard(degree: String, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(
                colors = listOf(Color(0xFF1E1E1E), Color(0xFF121212))
            ))
            .border(1.dp, SinoWhite.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CURRENT PLAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = SinoWhite.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = degree,
                    style = MaterialTheme.typography.titleMedium,
                    color = SinoWhite,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF50C878),
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SinoWhite.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF50C878))
            )
        }
    }
}

@Composable
fun ProfileDetailItem(iconRes: Int, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = SinoWhite.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SinoWhite.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = SinoWhite
            )
        }
    }
}