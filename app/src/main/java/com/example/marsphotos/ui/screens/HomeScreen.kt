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
package com.example.marsphotos.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.example.marsphotos.R
import com.example.marsphotos.network.parseFeeds
import com.example.marsphotos.ui.theme.MarsPhotosTheme
import java.io.InputStream
import androidx.compose.ui.platform.LocalResources
import com.example.marsphotos.databases.Feed
import com.example.marsphotos.databases.FeedDatabase


@Composable
fun HomeScreen(
    marsUiState: MarsViewModel.MarsUiState,
    modifier: Modifier = Modifier,
    isStoriesPressed: Boolean,
    feeds: List<Feed>
) {
    if(LocalInspectionMode.current) {
        when (marsUiState) {
            is MarsViewModel.MarsUiState.Loading -> LoadingScreenPreview()
            is MarsViewModel.MarsUiState.Success -> {
                if(isStoriesPressed)
                    ResultScreenPreview()
                else
                    FeedScreenPreview()
            }
            is MarsViewModel.MarsUiState.Error -> ErrorScreenPreview()
        }
    }
    else
    {
        when (marsUiState) {
            is MarsViewModel.MarsUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
            is MarsViewModel.MarsUiState.Success -> {
                if(isStoriesPressed)
                    ResultScreen(
                        marsUiState.photos, modifier = modifier.fillMaxWidth()
                    )
                else
                    FeedScreen(feeds)
            }

            is MarsViewModel.MarsUiState.Error -> ErrorScreen(modifier = modifier.fillMaxSize())
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        )
    )
    Image(
        modifier = modifier.size(200.dp).graphicsLayer {rotationZ = angle},
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

/**
 * ResultScreen displaying number of photos retrieved.
 */
@Composable
fun ResultScreen(photos: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Box(
//        contentAlignment = Alignment.Center,
//        modifier = modifier
//        ) {
//            Text(text = photos)
//        }
        val entries = parseFeeds(photos)
        items(entries){
            Article(it.title, it.summary, it.link)
        }
    }
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun Article(title: String?, summary: String?, link: String?) {
    val myModifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .padding(horizontal = 8.dp, vertical = 8.dp)
//        .size(width=240.dp, height=100.dp)
//        .verticalScroll(rememberScrollState())
    Card(colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
        modifier = myModifier
    ){
        Column(Modifier)
        {
            Text(
                text = title.toString(),
                modifier = myModifier,
                textAlign = TextAlign.Left,
            )
            Text(
                text = HtmlCompat.fromHtml(summary.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),//.split(" ").subList(0, 20).joinToString(" ").plus("..."),
                modifier = myModifier,
                textAlign = TextAlign.Left,
            )
            Text(
                text = link.toString(),
                modifier = myModifier,
                textAlign = TextAlign.Left,
            )}
    }
}

@Composable
fun FeedScreen(feeds: List<Feed>)
{
    var text by remember { mutableStateOf("Hello") }
    LazyColumn(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(feeds){
            it.link?.let { text -> Text(text) }
        }

        item{
            TextField(
                value = text,
                onValueChange = { text = it}
            )
        }
        item{
            Button(
                onClick = { FeedDatabase.getInstance()!!.feedDao().insertAll(Feed(link = text)) }
            ){
                Text("Add")
            }}
    }


}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    MarsPhotosTheme {
        val exampleRssFile : InputStream = LocalResources.current.openRawResource(R.raw.example_rss)
        val exampleRssFileAsString = exampleRssFile.bufferedReader().use { it.readText() }
        ResultScreen(listOf(exampleRssFileAsString))

    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview(){
    FeedScreen(emptyList())
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview(){
    LoadingScreen()
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview(){
    ErrorScreen()
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(marsUiState: MarsViewModel.MarsUiState = MarsViewModel.MarsUiState.Loading){
    MarsPhotosTheme{
        HomeScreen(marsUiState, isStoriesPressed = true, feeds = emptyList())
    }
}




