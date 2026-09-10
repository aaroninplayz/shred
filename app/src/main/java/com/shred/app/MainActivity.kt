package com.shred.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.shred.app.data.model.AppSettings
import com.shred.app.ui.navigation.ShredNavGraph
import com.shred.app.ui.theme.LocalShredColors
import com.shred.app.ui.theme.ShredTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ShredApplication

        setContent {
            val settings by app.settingsRepository.getSettingsFlow().collectAsState(initial = AppSettings())
            var hasStoragePermission by remember { mutableStateOf(checkStoragePermission()) }

            LaunchedEffect(Unit) {
                hasStoragePermission = checkStoragePermission()
            }

            ShredTheme(themeMode = settings.themeMode) {
                if (!hasStoragePermission) {
                    PermissionRequiredScreen(
                        onRequestPermission = {
                            requestStoragePermission()
                        },
                        onCheckAgain = {
                            hasStoragePermission = checkStoragePermission()
                        }
                    )
                } else {
                    val navController = rememberNavController()
                    ShredNavGraph(navController = navController, app = app)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when returning from system settings
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
    }
}

@Composable
fun PermissionRequiredScreen(
    onRequestPermission: () -> Unit,
    onCheckAgain: () -> Unit
) {
    val colors = LocalShredColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, colors.border)
                .background(colors.cardBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ACCESS REQUIRED ///",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SHRED NEEDS FULL STORAGE ACCESS TO DISCOVER AND PURGE UNWANTED PHOTOS, VIDEOS, AND AUDIO FILES IN HIDDEN DIRECTORIES, WHATSAPP, TELEGRAM, AND NON-INDEXED SECTORS.",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, colors.accent)
                    .background(colors.surface)
                    .clickable { onRequestPermission() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ GRANT ALL FILES ACCESS ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = colors.accent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, colors.borderSubtle)
                    .clickable { onCheckAgain() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ I HAVE GRANTED ACCESS ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = colors.textPrimary
                )
            }
        }
    }
}
