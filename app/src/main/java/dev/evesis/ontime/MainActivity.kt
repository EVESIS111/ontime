package dev.evesis.ontime

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.evesis.ontime.data.ReminderRepository
import dev.evesis.ontime.ui.home.HomeScreen
import dev.evesis.ontime.ui.home.HomeViewModel
import dev.evesis.ontime.ui.theme.OnTimeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        viewModelFactory {
            initializer { HomeViewModel(ReminderRepository.get(application)) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Channels.ensure(this)
        VoicePacks.installBundled(this)
        Sounds.ensureInstalled(this)
        Db.get(this).seedDefaultsIfEmpty()
        Alarms.scheduleAll(this)
        KeepAliveService.start(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            OnTimeTheme {
                HomeScreen(viewModel)
            }
        }
    }

    /** 提醒 App 的通知是生命线;Android 13+ 需运行时请求,否则到点通知全静默 */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }
}
