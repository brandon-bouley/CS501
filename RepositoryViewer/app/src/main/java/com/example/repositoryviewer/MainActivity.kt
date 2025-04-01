package com.example.repositoryviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                    GitHubRepoScreen()

            }
        }
    }
}

@Composable
fun GitHubRepoScreen(viewModel: GitHubViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchHeader(username, onUsernameChange = { username = it },
            onSearch = { viewModel.searchRepositories(username) })

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error!!)
            else -> RepositoryList(state, viewModel::loadNextPage)
        }
    }
}

@Composable
private fun SearchHeader(
    username: String,
    onUsernameChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.weight(1f),
            label = { Text("GitHub Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = onSearch) {
            Text("Search")
        }
    }
}

@Composable
private fun RepositoryList(state: GitHubState, onLoadMore: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.repos) { repo ->
            RepositoryItem(repo)
        }

        item {
            if (state.hasMore) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isLoadingMore) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = onLoadMore,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Load More")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoryItem(repo: Repository) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = repo.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            repo.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LabelWithIcon(icon = Icons.Default.Star, text = "${repo.stars}")
                LabelWithIcon(icon = Icons.Default.Build, text = "${repo.forks}")
            }
        }
    }
}

@Composable
private fun LabelWithIcon(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text)
    }
}

class GitHubViewModel : ViewModel() {
    private val _state = MutableStateFlow(GitHubState())
    val state: StateFlow<GitHubState> = _state

    private var currentUsername: String? = null

    fun searchRepositories(username: String) {
        if (username.isBlank()) return

        currentUsername = username
        _state.update {
            it.copy(
                repos = emptyList(),
                isLoading = true,
                error = null,
                nextPage = 1,
                hasMore = true
            )
        }

        viewModelScope.launch {
            try {
                fetchPage(1)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        val currentState = _state.value
        if (!currentState.hasMore || currentState.isLoadingMore) return

        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                fetchPage(currentState.nextPage)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private suspend fun fetchPage(page: Int) {
        val username = currentUsername ?: return
        val response = RetrofitInstance.api.getRepositories(username, page)

        if (response.isSuccessful) {
            val newRepos = response.body() ?: emptyList()
            val linkHeader = response.headers()["Link"]
            val hasMore = checkForNextPage(linkHeader)

            _state.update {
                it.copy(
                    repos = it.repos + newRepos,
                    isLoading = false,
                    isLoadingMore = false,
                    nextPage = page + 1,
                    hasMore = hasMore
                )
            }
        } else {
            throw IOException("Error: ${response.code()} ${response.message()}")
        }
    }

    private fun checkForNextPage(linkHeader: String?): Boolean {
        return linkHeader?.contains("rel=\"next\"") ?: false
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Red)
    }
}

data class GitHubState(
    val repos: List<Repository> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val nextPage: Int = 1,
    val hasMore: Boolean = false
)

data class Repository(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "html_url") val url: String,
    @Json(name = "stargazers_count") val stars: Int,
    @Json(name = "forks_count") val forks: Int
)

interface GitHubApiService {
    @GET("users/{username}/repos")
    suspend fun getRepositories(
        @Path("username") username: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 30
    ): Response<List<Repository>>
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.github.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    val api: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            ))
            .build()
            .create(GitHubApiService::class.java)
    }
}