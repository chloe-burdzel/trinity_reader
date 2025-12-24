/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.marsphotos.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.R
import com.example.marsphotos.ui.screens.HomeScreen
import com.example.marsphotos.ui.screens.MarsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.marsphotos.databases.Feed

@Composable
fun MarsPhotosApp() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isStoriesPressed by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { MarsTopAppBar(scrollBehavior = scrollBehavior) },
        bottomBar = {
            MarsBottomAppBar( {
                isStoriesPressed = false
            },{
                isStoriesPressed = true
            })
        }
    ) {
            innerPadding->Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        if(LocalInspectionMode.current){
            HomeScreen(
                marsUiState = MarsViewModel.MarsUiState.Success(emptyList()),
                isStoriesPressed = isStoriesPressed,
                feeds = emptyList()
            )
        }
        else{
            val marsViewModel: MarsViewModel = viewModel()

            val state by marsViewModel.marsUiState.collectAsStateWithLifecycle(initialValue = MarsViewModel.MarsUiState.Loading)
            val feeds: List<Feed> by marsViewModel.feeds.collectAsStateWithLifecycle()

            HomeScreen(
                marsUiState = state,
                isStoriesPressed = isStoriesPressed,
                feeds = feeds,
            )
        }


    }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MarsPhotosApp()
}

@Composable
fun MarsTopAppBar(scrollBehavior: TopAppBarScrollBehavior, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier
    )
}

@Composable
fun MarsBottomAppBar(onFeedClick: () -> Unit, onStoryClick: () -> Unit) {
    BottomAppBar (modifier = Modifier.padding(0.dp)){
        Row(modifier = Modifier.wrapContentSize()){
            Spacer(Modifier.weight(1.0f))
            MarsBottomAppButton("Feeds", R.drawable.feed_icon, onFeedClick)
            MarsBottomAppButton("Stories", R.drawable.story_icon, onStoryClick)
            Spacer(Modifier.weight(1.0f))
        }
    }
}

@Composable
fun MarsBottomAppButton(buttonText: String, @DrawableRes icon: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.wrapContentWidth().fillMaxHeight().background(Color.Transparent),
        colors = CardDefaults.cardColors(Color.Transparent)
    ){
        Column(
            modifier = Modifier.wrapContentSize().fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Image(
                painter = painterResource(icon),
                contentDescription = "Some description idk",
                modifier = Modifier.weight(1.0f),
                contentScale = ContentScale.Fit
            )
            Text (text = buttonText, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}