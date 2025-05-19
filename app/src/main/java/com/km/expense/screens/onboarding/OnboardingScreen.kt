package com.km.expense.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.km.expense.R
import com.km.expense.data.preferences.UserPreferences
import com.km.expense.navigation.AppNavigationActions
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(navigationActions: AppNavigationActions) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to ConstructionSite Manager",
            description = "A comprehensive solution for managing your construction site operations",
            imageRes = R.drawable.onboarding_welcome
        ),
        OnboardingPage(
            title = "Worker Management",
            description = "Track worker attendance, assign tasks, and monitor performance",
            imageRes = R.drawable.onboarding_workers
        ),
        OnboardingPage(
            title = "Equipment Tracking",
            description = "Manage your equipment inventory, track usage, and schedule maintenance",
            imageRes = R.drawable.onboarding_equipment
        ),
        OnboardingPage(
            title = "Progress Reporting",
            description = "Document site progress with photos and detailed reports",
            imageRes = R.drawable.onboarding_progress
        )
    )

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopSection(pagerState = pagerState, onSkipClick = {
            userPreferences.setFirstLaunch(false)
            navigationActions.navigateToAuth()
        })

        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { position ->
            PagerScreen(onboardingPage = pages[position])
        }

        BottomSection(
            pagerState = pagerState,
            onNextClick = {
                if (pagerState.currentPage + 1 < pages.size) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    userPreferences.setFirstLaunch(false)
                    navigationActions.navigateToAuth()
                }
            },
            onPreviousClick = {
                if (pagerState.currentPage > 0) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(pagerState.currentPage - 1)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TopSection(pagerState: PagerState, onSkipClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Back button - only show if not on first page
        AnimatedVisibility(
            visible = pagerState.currentPage > 0,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            IconButton(onClick = { /* Handle back navigation */ }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Back")
            }
        }

        // Skip button
        TextButton(
            onClick = onSkipClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = "Skip",
                color = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
fun PagerScreen(onboardingPage: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = onboardingPage.imageRes),
            contentDescription = "Onboarding Image",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = onboardingPage.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = onboardingPage.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BottomSection(
    pagerState: PagerState,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Page indicators
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.Center),
            activeColor = MaterialTheme.colors.primary,
            inactiveColor = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
        )

        // Previous button
        AnimatedVisibility(
            visible = pagerState.currentPage > 0,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colors.primary
                )
            }
        }

        // Next/Get Started button
        Button(
            onClick = onNextClick,
            modifier = Modifier.align(Alignment.CenterEnd),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text(
                text = if (pagerState.currentPage == pagerState.pageCount - 1) "Get Started" else "Next",
                color = Color.White
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = Color.White
            )
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)
