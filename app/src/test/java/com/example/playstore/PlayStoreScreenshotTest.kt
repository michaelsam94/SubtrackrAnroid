package com.example.playstore

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ScreenRoute
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SubscriptionViewModel
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Phone Play Store size: 1080×1920 px at xxhdpi (density 3). */
private const val PHONE_QUALIFIERS = "w360dp-h640dp-xxhdpi"

/** Tablet Play Store size: 1600×2560 px at xhdpi (density 2). */
private const val TABLET_QUALIFIERS = "w800dp-h1280dp-xhdpi"

/** Feature graphic: 1024×500 px at mdpi (1 dp = 1 px). */
private const val FEATURE_GRAPHIC_QUALIFIERS = "w1024dp-h500dp-mdpi"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayStoreScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var viewModel: SubscriptionViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        PlayStoreTestFixtures.resetAndSeed(application)
        viewModel = SubscriptionViewModel(application)
        waitForScreenshotData()
    }

    private fun waitForScreenshotData() {
        runBlocking {
            withTimeout(20_000) {
                while (viewModel.subscriptions.value.isEmpty()) {
                    Thread.sleep(50)
                }
                while (viewModel.isAnalyzing.value) {
                    Thread.sleep(50)
                }
            }
        }
    }

    private fun capturePhone(screen: ScreenRoute, fileName: String) {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                PlayStoreScreenshotFrame(selectedRoute = screen, viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "../play-store/phone/$fileName",
        )
    }

    private fun captureTablet(screen: ScreenRoute, fileName: String) {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                PlayStoreScreenshotFrame(selectedRoute = screen, viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "../play-store/tablet/$fileName",
        )
    }

    @Test
    @Config(qualifiers = PHONE_QUALIFIERS)
    fun phone_01_dashboard() = capturePhone(ScreenRoute.Dashboard, "01_dashboard.png")

    @Test
    @Config(qualifiers = PHONE_QUALIFIERS)
    fun phone_02_subscriptions() = capturePhone(ScreenRoute.Subscriptions, "02_subscriptions.png")

    @Test
    @Config(qualifiers = PHONE_QUALIFIERS)
    fun phone_03_ai_chat() = capturePhone(ScreenRoute.AIChat, "03_ai_chat.png")

    @Test
    @Config(qualifiers = PHONE_QUALIFIERS)
    fun phone_04_renewals() = capturePhone(ScreenRoute.Renewals, "04_renewals.png")

    @Test
    @Config(qualifiers = TABLET_QUALIFIERS)
    fun tablet_01_dashboard() = captureTablet(ScreenRoute.Dashboard, "01_dashboard.png")

    @Test
    @Config(qualifiers = TABLET_QUALIFIERS)
    fun tablet_02_subscriptions() = captureTablet(ScreenRoute.Subscriptions, "02_subscriptions.png")

    @Test
    @Config(qualifiers = TABLET_QUALIFIERS)
    fun tablet_03_ai_chat() = captureTablet(ScreenRoute.AIChat, "03_ai_chat.png")

    @Test
    @Config(qualifiers = TABLET_QUALIFIERS)
    fun tablet_04_renewals() = captureTablet(ScreenRoute.Renewals, "04_renewals.png")
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayStoreFeatureGraphicTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var viewModel: SubscriptionViewModel

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        PlayStoreTestFixtures.resetAndSeed(application)
        viewModel = SubscriptionViewModel(application)
        runBlocking {
            withTimeout(20_000) {
                while (viewModel.subscriptions.value.isEmpty()) {
                    Thread.sleep(50)
                }
                while (viewModel.isAnalyzing.value) {
                    Thread.sleep(50)
                }
            }
        }
    }

    @Test
    @Config(qualifiers = FEATURE_GRAPHIC_QUALIFIERS)
    fun feature_graphic_1024x500() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                FeatureGraphicContent(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "../play-store/feature-graphic.png",
        )
    }
}
