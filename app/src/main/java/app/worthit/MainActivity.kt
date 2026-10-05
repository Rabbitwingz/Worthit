package app.worthit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.worthit.ui.AppViewModel
import app.worthit.ui.WorthItApp
import app.worthit.ui.theme.WorthItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            val data by vm.data.collectAsStateWithLifecycle()
            WorthItTheme(dynamic = data.profile.dynamicColor) {
                WorthItApp(vm)
            }
        }
    }
}
