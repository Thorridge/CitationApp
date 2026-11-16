package com.example.citationapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var tvQuote: TextView
    private lateinit var tvAuthor: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Gonflement du design XML
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvQuote = view.findViewById(R.id.tvQuote)
        tvAuthor = view.findViewById(R.id.tvAuthor)

        // Charger la citation du jour via notre client
        loadQuoteOfTheDay()

        return view
    }

    private fun loadQuoteOfTheDay() {
        ZenQuotesClient.getQuoteOfTheDay(object : ZenQuotesClient.QuoteCallback {
            override fun onSuccess(quote: Quote) {
                if (isAdded) { // Vérifie que le fragment est bien affiché à l'écran
                    tvQuote.text = "\"${quote.text}\""
                    tvAuthor.text = "- ${quote.author}"
                }
            }
            override fun onError(errorMessage: String) {
                if (isAdded) {
                    tvQuote.text = errorMessage
                }
            }
        })
    }
}