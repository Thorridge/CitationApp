package com.example.citationapp

import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Modèle de données représentant une citation retournée par ZenQuotes.io
 */
data class Quote(
    val text: String,
    val author: String,
    val html: String
)

/**
 * Client réseau pour l'API ZenQuotes.io.
 * Compatible Android 7.0 (API 24+) sans aucune bibliothèque externe requise.
 */
object ZenQuotesClient {

    private const val BASE_URL = "https://zenquotes.io/api"
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Interface de retour pour récupérer le résultat dans votre Activity ou Fragment
     */
    interface QuoteCallback {
        fun onSuccess(quote: Quote)
        fun onError(errorMessage: String)
    }

    /**
     * Récupère la citation du jour (/api/today)
     */
    fun getQuoteOfTheDay(callback: QuoteCallback) {
        fetchSingleQuote("$BASE_URL/today", callback)
    }

    /**
     * Récupère une citation aléatoire (/api/random)
     */
    fun getRandomQuote(callback: QuoteCallback) {
        fetchSingleQuote("$BASE_URL/random", callback)
    }

    private fun fetchSingleQuote(endpointUrl: String, callback: QuoteCallback) {
        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(endpointUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 10000 // 10 secondes
                    readTimeout = 10000
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use {
                        it.readText()
                    }

                    // ZenQuotes renvoie un tableau JSON : [{"q":"...", "a":"...", "h":"..."}]
                    val jsonArray = JSONArray(response)
                    if (jsonArray.length() > 0) {
                        val item = jsonArray.getJSONObject(0)
                        val quote = Quote(
                            text = item.optString("q", ""),
                            author = item.optString("a", "Inconnu"),
                            html = item.optString("h", "")
                        )
                        mainHandler.post { callback.onSuccess(quote) }
                    } else {
                        mainHandler.post { callback.onError("Aucune citation reçue.") }
                    }
                } else {
                    mainHandler.post { callback.onError("Erreur serveur HTTP : $responseCode") }
                }
            } catch (e: Exception) {
                mainHandler.post { callback.onError("Erreur réseau : ${e.localizedMessage ?: "Inconnue"}") }
            } finally {
                connection?.disconnect()
            }
        }
    }
}