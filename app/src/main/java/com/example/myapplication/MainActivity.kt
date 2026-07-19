package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.communication.MusicRecommendationCodec
import com.example.myapplication.communication.MusicRecommendationInbox

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicRecommendationInbox.initialize(this)
        MusicRecommendationCodec.fromIntent(intent)?.let {
            MusicRecommendationInbox.deliver(this, it)
        }

        setContent {
            val receivedRecommendation by MusicRecommendationInbox.recommendation.collectAsState()
            App(
                receivedRecommendation = receivedRecommendation,
                onRecommendationConsumed = {
                    MusicRecommendationInbox.consume(this, it)
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        MusicRecommendationCodec.fromIntent(intent)?.let {
            MusicRecommendationInbox.deliver(this, it)
        }
    }
}
