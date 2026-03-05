package com.example.invyucab_project.mainui.profilescreen.memberlevelscreen.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.invyucab_project.mainui.profilescreen.memberlevelscreen.viewmodel.MemberLevel
import com.example.invyucab_project.mainui.profilescreen.memberlevelscreen.viewmodel.MemberLevelViewModel
import com.example.invyucab_project.ui.theme.CabMintGreen
import com.example.invyucab_project.ui.theme.CabVeryLightMint
import com.example.invyucab_project.ui.theme.LightSlateGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberLevelScreen(
    navController: NavController,
    viewModel: MemberLevelViewModel = hiltViewModel()
) {
    val levels by viewModel.levels.collectAsState()
    val currentUserLevel by viewModel.currentUserLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Levels", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CabMintGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = LightSlateGray.copy(alpha = 0.5f)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(levels) { level ->
                LevelCard(
                    level = level,
                    isCurrentLevel = level.level == currentUserLevel
                )
            }
        }
    }
}

@Composable
fun LevelCard(level: MemberLevel, isCurrentLevel: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentLevel) CabVeryLightMint else Color.White
        ),
        border = if (isCurrentLevel) BorderStroke(2.dp, CabMintGreen) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = level.icon,
                contentDescription = level.level,
                modifier = Modifier
                    .size(40.dp)
                    .background(level.iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                tint = level.iconColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.level,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = level.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            if (isCurrentLevel) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Current",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CabMintGreen,
                    modifier = Modifier
                        .background(CabMintGreen.copy(alpha = 0.1f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}