package com.km.expense.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.km.expense.R
import com.km.expense.data.preferences.UserPreferences
import com.km.expense.navigation.AppNavigationActions
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navigationActions: AppNavigationActions) {
    val userPreferences = remember { UserPreferences() }
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        if (userPreferences.isFirstLaunch()) {
            navigationActions.navigateToOnboarding()
        } else if (userPreferences.isLoggedIn()) {
            navigationActions.navigateToDashboard()
        } else {
            navigationActions.navigateToAuth()
        }
    }

    Splash(alpha = alphaAnim.value)
}

@Composable
fun Splash(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                modifier = Modifier
                    .size(200.dp)
                    .alpha(alpha),
                painter = painterResource(id = R.drawable.ic_construction_logo),
                contentDescription = "Logo"
            )
            
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(alpha),
                text = stringResource(id = R.string.app_name),
                color = MaterialTheme.colors.onPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
