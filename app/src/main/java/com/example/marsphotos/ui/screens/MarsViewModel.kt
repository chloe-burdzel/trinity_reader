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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.databases.Feed
import com.example.marsphotos.databases.FeedDatabase
import com.example.marsphotos.network.MarsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class MarsViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */

    sealed interface MarsUiState {
        data class Success(val photos: List<String>) : MarsUiState
        object Loading : MarsUiState
        object Error: MarsUiState
    }

    private var _marsUiState = MutableStateFlow<MarsUiState>(MarsUiState.Loading)
    var marsUiState: StateFlow<MarsUiState> = _marsUiState.asStateFlow()
        private set

    val feeds: StateFlow<List<Feed>> = FeedDatabase.getInstance()!!.feedDao().getAllAsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
        getMarsPhotos()
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [MutableList].
     */
    private fun getMarsPhotos() {
        viewModelScope.launch {
            val db = FeedDatabase.getInstance()
            if(db == null){
                _marsUiState.update { MarsUiState.Error }
            }
            else{
                val feedDao = db.feedDao()
                val feeds: List<Feed> = feedDao.getAll()

                val photos = mutableListOf<String>()
                for(feed in feeds) {
                    try {
                        photos.add(MarsApi.retrofitService.getPhotos(feed.link.toString()))
                    } catch (e: Exception) {
                        println(e.stackTrace)
                        db.feedDao().delete(feed)
                        _marsUiState.update { MarsUiState.Error }
                    }
                }
                _marsUiState.update { MarsUiState.Success(photos) }
            }
        }
    }
}