package com.example.pubcrawl

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pubcrawl.ui.theme.PubCrawlTheme
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.*

//Main
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PubCrawlTheme {
                PubCrawlApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun PubCrawlApp(modifier: Modifier = Modifier) {
    var showOnboardPage by rememberSaveable { mutableStateOf(true) }
    val navController = rememberNavController()
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    if (showOnboardPage) {
        OnboardingPage(
            enterAppClicked = { showOnboardPage = false },
            modifier = modifier
        )
    } else {
        LaunchedEffect(navController) {
            navController.addOnDestinationChangedListener { _, destination, _ ->
                selectedIndex = when (destination.route) {
                    "home" -> 0
                    "gemini" -> 1
                    "pub" -> 2
                    "crawl" -> 3
                    else -> 0
                }
            }
        }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    items = listOf("Home", "Pub Guide", "Pub", "Crawl"),
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        selectedIndex = index
                        when (index) {
                            0 -> navController.navigate("home")
                            1 -> navController.navigate("gemini")
                            2 -> navController.navigate("pub")
                            3 -> navController.navigate("crawl")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Surface(modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        selectedIndex = 0
                        HomeScreen(navController)
                    }
                    composable("gemini") {
                        selectedIndex = 1
                        GeminiScreen(modifier = Modifier)
                    }
                    composable("pub") {
                        selectedIndex = 2
                        PubScreen(bars)
                    }
                    composable("crawl") {
                        selectedIndex = 3
                        CrawlScreen(bars)
                    }
                }
            }
        }
    }
}



// Onboarding Page
@Composable
fun OnboardingPage(enterAppClicked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pub Crawl",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF6C2FAB))
        Button(
            modifier = Modifier.padding(vertical = 24.dp)
                .size(200.dp, 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C2FAB),
                contentColor = Color.White
            ),
            onClick = enterAppClicked
        ) {
            Text("Enter App")
        }
    }
}

@Composable
fun NavigationBar(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        color = Color(0xFF6C2FAB),
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround

        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Text(
                    text = item,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .clickable { onItemSelected(index) }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(navController : NavController) {
    var showMoreGemini by remember { mutableStateOf(false) }
    var showMorePub by remember { mutableStateOf(false) }
    var showMoreCrawl by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Plan your Pub Crawl",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFF6C2FAB)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFBE8ED0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Try our new Pub Guide",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                        .padding(top = 6.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Powered by Google Gemini AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (showMoreGemini) {
                    Text(
                        text = "Enter a prompt to get started. Pub Guide will come in help immediately and add the bars to your Pub Crawl list!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(all = 10.dp)
                    )
                    ElevatedButton(
                        onClick = {
                            navController.navigate("gemini")
                        },
                        modifier = Modifier.padding(all = 10.dp)
                            .align(Alignment.End)
                    ) {
                        Text("Try it now!")
                    }
                }

                ElevatedButton(
                    onClick = { showMoreGemini = !showMoreGemini },
                    modifier = Modifier.padding(all = 10.dp)
                        .align(Alignment.End)
                ) {
                    Text(if (showMoreGemini) "Show Less" else "Show More")
                }

            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF9B59B6)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Find the searched bars in the Pub section",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                        .padding(top = 10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (showMorePub) {
                    Text(
                        text = "Pub Guide will search for bars based on your preferences and add them to the Pub Section.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(all = 10.dp)
                    )
                    ElevatedButton(
                        onClick = {
                            navController.navigate("pub")
                        },
                        modifier = Modifier.padding(all = 10.dp)
                            .align(Alignment.End)
                    ) {
                        Text("Check the bars now!")
                    }
                }

                ElevatedButton(
                    onClick = { showMorePub = !showMorePub },
                    modifier = Modifier.padding(all = 10.dp)
                        .align(Alignment.End)
                ) {
                    Text(if (showMorePub) "Show Less" else "Show More")
                }

            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF8641D9)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Check the map in the Crawl section",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                        .padding(top = 10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (showMoreCrawl) {
                    Text(
                        text = "All bars added to the Pub Section will be displayed on the Crawl Map.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.padding(all = 10.dp)
                    )
                    ElevatedButton(
                        onClick = {
                            navController.navigate("crawl")
                        },
                        modifier = Modifier.padding(all = 10.dp)
                            .align(Alignment.End)
                    ) {
                        Text("Check the map now!")
                    }
                }

                ElevatedButton(
                    onClick = { showMoreCrawl = !showMoreCrawl },
                    modifier = Modifier.padding(all = 10.dp)
                        .align(Alignment.End)
                ) {
                    Text(if (showMoreCrawl) "Show Less" else "Show More")
                }
            }
        }
    }
}

//GEMINI SCREEN IMPLEMENTATION STARTS HERE

sealed class Message {
    data class TextFromYou(val content: String) : Message()
    data class TextFromGemini(val content: String) : Message()
    data object GeminiIsThinking : Message()
}

private val _messageHistory = mutableStateListOf<Message>()
val messageHistory: List<Message> get() = _messageHistory

private val _bars = mutableStateListOf<Bar>()
val bars: List<Bar> get() = _bars

class TextGeminiViewModel : ViewModel() {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-002",
        apiKey = BuildConfig.API_KEY,
        systemInstruction = content {
            text(GeminiSystemInstructions.systemInstructions)
        }
    )

    fun makePrompt(prompt: String) {
        _messageHistory.add(Message.TextFromYou(prompt))
        _messageHistory.add(Message.GeminiIsThinking)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = generativeModel.generateContent(prompt)

                withContext(Dispatchers.Main) {
                    _messageHistory.removeLast()

                    val processedText = result.text?.lines()
                        ?.drop(1)
                        ?.dropLast(2)
                        ?.joinToString("\n")

                    processedText?.let { responseText ->
                        _messageHistory.add(Message.TextFromGemini(responseText))

                        val newBars = parseBarsAsync(_messageHistory)
                        _bars.addAll(newBars.filter { newBar -> _bars.none { it.id == newBar.id } })
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _messageHistory.removeLast()
                    _messageHistory.add(Message.TextFromGemini("An error occurred. Please try again."))
                }
            }
        }

    }

}

@Composable
fun GeminiScreen(modifier: Modifier = Modifier) {
    val viewModel: TextGeminiViewModel = viewModel()
    var currentInput by remember { mutableStateOf("") }

    if (messageHistory.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Where would you like to go today?",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF6C2FAB),
                textAlign = TextAlign.Center
            )
        }
    }
        Column(modifier = modifier.fillMaxSize()) {
            Chat(messages = messageHistory, modifier = Modifier.weight(1f))

            BottomBar(
                currentInput = currentInput,
                onInputChange = { currentInput = it },
                onSend = { input ->
                    if (input.isNotBlank()) {
                        viewModel.makePrompt(input)
                    }
                },
                modifier = Modifier.padding(8.dp)
            )
        }
}

@Composable
fun Chat(messages: List<Message>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val state = rememberLazyListState()

        LaunchedEffect(messages) {
            delay(100)
            state.animateScrollToItem(messages.size)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                when (message) {
                    is Message.TextFromYou -> YourSpeechBubble(content = message.content)
                    is Message.TextFromGemini -> GeminiSpeechBubble(content = parseGeminiResponse(message).content)
                    is Message.GeminiIsThinking -> GeminiSpeechBubble(content = "Gemini is thinking...")
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    onSend: (String) -> Unit,
    currentInput: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = currentInput,
            onValueChange = onInputChange,
            placeholder = { Text("Input your prompt") },
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                if (currentInput.isNotBlank()) {
                    onSend(currentInput)
                    onInputChange("")
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message"
            )
        }
    }
}

@Composable
fun YourSpeechBubble(content: String, modifier: Modifier = Modifier) {
    SpeechBubble(
        content = content,
        author = "You",
        containerColor = Color(0xFF8A2BE2),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.padding(start = 30.dp, end = 0.dp)
    )
}

@Composable
fun GeminiSpeechBubble(content: String, modifier: Modifier = Modifier) {
    SpeechBubble(
        content = content,
        author = "Gemini",
        containerColor = Color(0xFF9B59B6),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.padding(start = 0.dp, end = 30.dp)
    )
}


@Composable
fun SpeechBubble(
    content: String,
    author: String,
    containerColor: Color,
    contentColor: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = author,
            fontSize = 12.sp,
            color = Color(0xFFD7A8E5),
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor),
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = content,
                color = contentColor,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun PubScreen(bars: List<Bar>) {
    if (bars.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ask Pub Guide for pubs",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF6C2FAB),
                textAlign = TextAlign.Center
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(bars) { bar ->
            PubCard(bar = bar)
        }
    }
}

@Composable
fun PubCard(bar: Bar) {
    var showMore by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    isSelected = bar.isChecked
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { isSelected = !isSelected },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF6A0DAD) else Color(0xFFBE8ED0)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bar.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            if (showMore) {
                Text(
                    text = "Description: ${bar.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Price: ${bar.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rating: ${bar.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ElevatedButton(
                    onClick = { showMore = !showMore },
                ) {
                    Text(if (showMore) "Show Less" else "Show More")
                }
            }
        }
    }
}


data class Bar(
    val id: Int,
    val name: String,
    val location: GeoPoint,
    val rating: Float,
    val price: String,
    val description: String,
    var isChecked: Boolean
)

fun parseBars(messageHistory: List<Message>): List<Bar> {

    try {
        val jsonString = (messageHistory.last() as? Message.TextFromGemini)?.content ?: ""

        if (jsonString.isNotBlank()) {
            val jsonObject = JSONObject(jsonString)

            val barsArray = jsonObject.optJSONArray("bars") ?: JSONArray()

            for (i in 0 until barsArray.length()) {
                val barObject = barsArray.getJSONObject(i)

                val locationObject = barObject.optJSONObject("location")
                val location = GeoPoint(
                    locationObject?.optDouble("lat", 0.0) ?: 0.0,
                    locationObject?.optDouble("lng", 0.0) ?: 0.0
                )

                val bar = Bar(
                    id = barObject.optInt("id", -1),
                    name = barObject.optString("name", "Unknown"),
                    location = location,
                    rating = barObject.optDouble("rating", 0.0).toFloat(),
                    price = barObject.optString("price", "Unknown"),
                    description = barObject.optString("description", ""),
                    isChecked = barObject.optBoolean("isChecked")
                )

                val existingIndex = _bars.indexOfFirst { it.name == bar.name }
                if (existingIndex != -1) {
                    _bars[existingIndex] = bar
                    Log.i("PubCrawl", "Bar with name '${bar.name}' overwritten.")
                } else {
                    _bars.add(bar)
                }
            }
        } else {
            Log.e("PubCrawl", "Empty or invalid JSON string")
        }
    } catch (e: JSONException) {
        Log.e("PubCrawl", "Error parsing JSON: ${e.message}")
    } catch (e: Exception) {
        Log.e("PubCrawl", "Error parsing bars: ${e.message}")
    }

    return _bars
}


fun parseBarsAsync(messageHistory: List<Message>): List<Bar> {
    return runBlocking {
        withContext(Dispatchers.Default) {
            parseBars(messageHistory)
        }
    }
}

fun parseGeminiResponse(message: Message.TextFromGemini): Message.TextFromGemini {
    try {
        val jsonString = message.content

        if (jsonString.isNotBlank()) {
            val jsonObject = JSONObject(jsonString)

            val geminiResponse = jsonObject.optString("response", "")

            return Message.TextFromGemini(geminiResponse)
        } else {
            Log.e("PubCrawl", "Empty or invalid JSON string")
        }
    } catch (e: JSONException) {
        Log.e("PubCrawl", "Error parsing JSON: ${e.message}")
    } catch (e: Exception) {
        Log.e("PubCrawl", "Error parsing response: ${e.message}")
    }
    return Message.TextFromGemini("Error parsing response")
}

@Composable
fun CrawlScreen(bars: List<Bar>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OSMMap(
            bars = bars,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

@Composable
fun OSMMap(bars: List<Bar>, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            val mapView = MapView(context)
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osm_prefs", AppCompatActivity.MODE_PRIVATE)
            )
            mapView.setTileSource(TileSourceFactory.MAPNIK)

            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    mapView.controller.setZoom(15.0)
                    val startPoint = GeoPoint(44.4353, 26.0465)
                    mapView.controller.setCenter(startPoint)

                    val marker = Marker(mapView)
                    marker.position = startPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val drawable = ContextCompat.getDrawable(context, R.drawable.home)
                    if (drawable != null) {
                        val originalBitmap = (drawable as BitmapDrawable).bitmap
                        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 25, 25, false)
                        marker.icon = BitmapDrawable(context.resources, resizedBitmap)
                        Log.d("MarkerSetup", "S-a pus marker")
                    } else {
                        Log.e("MarkerSetup", "Drawable resource not found: R.drawable.pin")
                    }
                    mapView.overlays.add(marker)

                    val checkedBars = bars.filter { it.isChecked }

                    if (checkedBars.isEmpty()) return@withContext

                    val visited = IntArray(checkedBars.size) { 0 }
                    val path = mutableListOf<GeoPoint>()

                    path.add(startPoint)

                    fun DistanceCalculator(
                        point1: GeoPoint,
                        point2: GeoPoint
                    ): Double {
                        val earthRadius = 6371

                        val dLat = Math.toRadians(point2.latitude - point1.latitude)
                        val dLon = Math.toRadians(point2.longitude - point1.longitude)

                        val a = sin(dLat / 2) * sin(dLat / 2) +
                                cos(Math.toRadians(point1.latitude)) * cos(Math.toRadians(point2.latitude)) *
                                sin(dLon / 2) * sin(dLon / 2)

                        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

                        return earthRadius * c
                    }

                    fun quickestPath(startPoint: GeoPoint, checkedBars: List<Bar>, visited: IntArray, path: MutableList<GeoPoint>) {
                        var n = 0
                        var closestBar = checkedBars[0]
                        var minDistance = Double.MAX_VALUE

                        for (i in checkedBars.indices) {
                            if (visited[i] == 0) {
                                val currentDistance = DistanceCalculator(startPoint, checkedBars[i].location)
                                if (currentDistance < minDistance) {
                                    minDistance = currentDistance
                                    closestBar = checkedBars[i]
                                    n = i
                                }
                            }
                        }

                        visited[n] = 1
                        path.add(closestBar.location)

                        if (!visited.all { it == 1 }) {
                            quickestPath(closestBar.location, checkedBars, visited, path)
                        }
                    }

                    quickestPath(startPoint, checkedBars, visited, path)

                    val polyline = Polyline(mapView)
                    path.forEach { point ->
                        polyline.addPoint(point)
                    }
                    polyline.color = 0xFF000000.toInt()
                    polyline.width = 5f
                    mapView.overlays.add(polyline)
                }

                bars.forEach { bar ->
                    val marker = Marker(mapView)
                    marker.position = bar.location
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "${bar.name} - Rating: ${bar.rating}"

                    if (bar.isChecked) {
                        val drawable = ContextCompat.getDrawable(context, R.drawable.pinverdebun)
                        if (drawable != null) {
                            val originalBitmap = (drawable as BitmapDrawable).bitmap
                            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 50, 50, false)
                            marker.icon = BitmapDrawable(context.resources, resizedBitmap)
                            Log.d("MarkerSetup", "S-a pus marker")
                        } else {
                            Log.e("MarkerSetup", "Drawable resource not found: R.drawable.pin")
                        }
                    }
                    else{
                        val drawable = ContextCompat.getDrawable(context, R.drawable.pin)
                        if (drawable != null) {
                            val originalBitmap = (drawable as BitmapDrawable).bitmap
                            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 50, 50, false)
                            marker.icon = BitmapDrawable(context.resources, resizedBitmap)
                            Log.d("MarkerSetup", "S-a pus marker")
                        } else {
                            Log.e("MarkerSetup", "Drawable resource not found: R.drawable.pin")
                        }
                    }
                    mapView.overlays.add(marker)
                }
            }

            mapView
        },
        modifier = modifier
    )
}

