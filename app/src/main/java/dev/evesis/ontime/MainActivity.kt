package dev.evesis.ontime

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.evesis.ontime.data.ReminderRepository
import dev.evesis.ontime.ui.editor.EditorScreen
import dev.evesis.ontime.ui.editor.EditorViewModel
import dev.evesis.ontime.ui.home.HomeScreen
import dev.evesis.ontime.ui.home.HomeViewModel
import dev.evesis.ontime.ui.catalog.CatalogScreen
import dev.evesis.ontime.ui.settings.SettingsScreen
import dev.evesis.ontime.ui.theme.OnTimeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels {
        viewModelFactory {
            initializer { HomeViewModel(ReminderRepository.get(application)) }
        }
    }

    private val editorViewModel: EditorViewModel by viewModels {
        viewModelFactory {
            initializer { EditorViewModel(ReminderRepository.get(application)) }
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
                val nav = rememberNavController()
                // Instant Pixel Policy(v11.2):导航零动画——点击即到,返回即回
                NavHost(
                    navController = nav,
                    startDestination = "home",
                    enterTransition = { androidx.compose.animation.EnterTransition.None },
                    exitTransition = { androidx.compose.animation.ExitTransition.None },
                    popEnterTransition = { androidx.compose.animation.EnterTransition.None },
                    popExitTransition = { androidx.compose.animation.ExitTransition.None },
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onEdit = { id -> nav.navigate("editor/$id") },
                            onAdd = { nav.navigate("editor/0") },
                            onSettings = { nav.navigate("settings") },
                        )
                    }
                    composable(
                        route = "editor/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType }),
                    ) { entry ->
                        val id = entry.arguments?.getLong("id") ?: 0L
                        EditorScreen(
                            viewModel = editorViewModel,
                            id = id,
                            onDone = {
                                viewModel.refresh()
                                nav.popBackStack()
                            },
                        )
                    }
                    composable("settings") {
                        SettingsScreen(onBack = { nav.popBackStack() }, onCatalog = { nav.navigate("catalog") })
                    }
                    composable("catalog") {
                        CatalogScreen(onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }

    /** 提醒 App 的通知是生命线;Android 13+ 需运行时请求,否则到点通知全静默。附带语音包导入的音频读取 */
    private fun requestNotificationPermissionIfNeeded() {
        val wanted = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                wanted.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                wanted.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            wanted.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (wanted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, wanted.toTypedArray(), 100)
        }
    }
}
