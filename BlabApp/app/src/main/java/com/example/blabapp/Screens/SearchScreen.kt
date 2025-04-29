package com.example.blabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.blabapp.Repository.UserRepository
import com.example.blabapp.translateSentence
import com.example.blabapp.translateSentenceFromEStoEN
import com.example.blabapp.ui.theme.BlabAppTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    val viewModel: DictionaryViewModel = viewModel()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var userLearning by remember { mutableStateOf("EN") }
    val coroutineScope = rememberCoroutineScope()
    val searchLabel by remember(userLearning) {
        derivedStateOf {
            if (userLearning == "ES") "Search in English" else "Buscar en Español"
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            UserRepository.getUser()?.let { user ->
                userLearning = user.learning
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Purple header with search
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = {
                    Text(
                        text = searchLabel,
                        color = MaterialTheme.colorScheme.surface
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.searchDefinitions(searchQuery) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.searchDefinitions(searchQuery) }
                )
            )
        }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val wordLabel = if (userLearning == "EN") "Palabra:" else "Word:"
            val partOfSpeechLabel = if (userLearning == "EN") "Parte del discurso:" else "Part of Speech:"
            val exampleLabel = if (userLearning == "EN") "Ejemplo:" else "Example:"

            when {
                viewModel.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (userLearning == "EN") "Buscando..." else "Searching...",
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                viewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error: ${viewModel.errorMessage}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                viewModel.definitions.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(viewModel.definitions) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {

                                    var translatedSearchWord by remember { mutableStateOf(entry.word) }

                                    if(userLearning == "ES"){
                                        LaunchedEffect(translatedSearchWord) {
                                            translateSentence(translatedSearchWord) { translated ->
                                                translatedSearchWord = translated
                                            }
                                        }
                                    }
                                    Text(
                                        text = "$wordLabel ${translatedSearchWord}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    entry.meanings.forEach { meaning ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "$partOfSpeechLabel ${meaning.partOfSpeech}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        meaning.definitions.forEachIndexed { index, def ->
                                            var translatedDefinition by remember { mutableStateOf(def.definition) }
                                            var translatedExample by remember { mutableStateOf(def.example ?: "") }

                                            if (userLearning == "EN") {
                                                LaunchedEffect(def.definition) {
                                                    translateSentence(def.definition) { translated ->
                                                        translatedDefinition = translated
                                                    }
                                                }
                                                def.example?.let { example ->
                                                    LaunchedEffect(example){
                                                            translatedExample = example
                                                    }
                                                }
                                            }else{
                                                def.example?.let { example ->
                                                    LaunchedEffect(example) {
                                                        translateSentence(example) { translated ->
                                                            translatedExample = translated
                                                        }
                                                    }
                                                }
                                            }

                                            Text(
                                                text = "${index + 1}. $translatedDefinition",
                                                modifier = Modifier.padding(top = 4.dp, start = 8.dp),
                                                color = MaterialTheme.colorScheme.secondary
                                            )

                                            if (translatedExample.isNotBlank()) {
                                                Text(
                                                    text = "$exampleLabel \"$translatedExample\"",
                                                    modifier = Modifier.padding(top = 10.dp, start = 25.dp),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




class DictionaryViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage: String? by mutableStateOf(null)
    var definitions by mutableStateOf<List<WordEntry>>(emptyList())

    private val api = DictionaryApi.retrofit

    var userLearning by mutableStateOf("EN")

    fun searchDefinitions(word: String) {
        if (word.isBlank()) return
        isLoading = true
        errorMessage = null

        kotlinx.coroutines.GlobalScope.launch {
            try {
                var queryWord = word.lowercase()

                if (userLearning == "EN") {
                    val translated = suspendTranslateSentence(word)
                    if (translated.isNotBlank()) {
                        queryWord = translated.lowercase()
                    }
                }

                val response = api.getDefinitions(queryWord)

                var processedResponse = response

                if (userLearning == "EN") {
                    processedResponse = response.map { entry ->
                        entry.copy(
                            word = suspendTranslateSentence(entry.word),
                            meanings = entry.meanings.map { meaning ->
                                meaning.copy(
                                    partOfSpeech = suspendTranslateSentence(meaning.partOfSpeech),
                                    definitions = meaning.definitions.map { def ->
                                        def.copy(
                                            definition = suspendTranslateSentence(def.definition),
                                            example = def.example?.let { suspendTranslateSentence(it) }
                                        )
                                    },
                                    synonyms = meaning.synonyms.map { suspendTranslateSentence(it) },
                                    antonyms = meaning.antonyms.map { suspendTranslateSentence(it) }
                                )
                            }
                        )
                    }
                }

                definitions = processedResponse
                isLoading = false

            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unknown error"
                isLoading = false
            }
        }
    }
}

suspend fun suspendTranslateSentence(text: String): String {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        translateSentenceFromEStoEN(text) { translated ->
            continuation.resume(translated, null)
        }
    }
}

interface DictionaryApiService {
    @GET("api/v2/entries/en/{word}")
    suspend fun getDefinitions(@Path("word") word: String): List<WordEntry>
}

object DictionaryApi {
    val retrofit: DictionaryApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApiService::class.java)
    }
}

data class WordEntry(
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic>?,
    val meanings: List<Meaning>,
    val license: License?,
    val sourceUrls: List<String>?
)

data class Phonetic(
    val text: String?,
    val audio: String?,
    val sourceUrl: String?,
    val license: License?
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>,
    val synonyms: List<String>,
    val antonyms: List<String>
)

data class Definition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)

data class License(
    val name: String,
    val url: String
)



@Preview(showBackground = true, name = "Empty State")
@Composable
fun SearchScreenPreview_Empty() {
    BlabAppTheme { // Use your app's theme here
        SearchScreen(navController = rememberNavController())
    }
}