package com.esteticapatri.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.esteticapatri.app.ui.navigation.MainScreen
import com.esteticapatri.app.ui.theme.EsteticaPatriTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appointmentId = intent?.getLongExtra("EXTRA_APPOINTMENT_ID", -1L)?.takeIf { it > 0 }

        setContent {
            EsteticaPatriTheme {
                MainScreen(initialAppointmentId = appointmentId)
            }
        }
    }
}
