package com.campus.library

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.campus.library.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent { CampusLibraryTheme { Surface(Modifier.fillMaxSize()) { CampusLibraryApp() } } }
    }
}

@Composable
fun CampusLibraryApp() {
    var route by rememberSaveable { mutableStateOf("catalog") }
    var bookId by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(route != "catalog") { route = "catalog" }
    when (route) {
        "detail" -> BookDetailScreen(bookId, { route = "catalog" }, { route = "loans" })
        "loans" -> MyLoansScreen(onBackClick = { route = "catalog" })
        else -> BookListScreen({ bookId = it; route = "detail" }, { route = "loans" })
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF354CB0), onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E6FF), onPrimaryContainer = Color(0xFF20347F),
    background = Color(0xFFF4F1F7), onBackground = Color(0xFF252130),
    surface = Color.White, onSurface = Color(0xFF252130),
    surfaceVariant = Color(0xFFECE7F0), onSurfaceVariant = Color(0xFF625A6D),
    outline = Color(0xFF7B7384), outlineVariant = Color(0xFFDCD5E3),
    secondary = Color(0xFF30243E), onSecondary = Color.White,
    secondaryContainer = Color(0xFF30243E), onSecondaryContainer = Color(0xFFF4F1F7)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFBBC3FF), onPrimary = Color(0xFF18296E),
    primaryContainer = Color(0xFF2B3E91), onPrimaryContainer = Color(0xFFE0E4FF),
    background = Color(0xFF19151F), onBackground = Color(0xFFECE5F2),
    surface = Color(0xFF231D2B), onSurface = Color(0xFFECE5F2),
    surfaceVariant = Color(0xFF362F3F), onSurfaceVariant = Color(0xFFCAC0D3),
    outline = Color(0xFF9B8FA7), outlineVariant = Color(0xFF4C4257),
    secondary = Color(0xFFD5BFE6), onSecondary = Color(0xFF30243E),
    secondaryContainer = Color(0xFF30243E), onSecondaryContainer = Color(0xFFF4F1F7)
)

@Composable
fun CampusLibraryTheme(content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = base.copy(
            headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif),
            headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif),
            headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif),
            titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif)
        ),
        shapes = Shapes(extraSmall = RoundedCornerShape(4.dp), small = RoundedCornerShape(4.dp), medium = RoundedCornerShape(6.dp), large = RoundedCornerShape(8.dp), extraLarge = RoundedCornerShape(8.dp)),
        content = content
    )
}
