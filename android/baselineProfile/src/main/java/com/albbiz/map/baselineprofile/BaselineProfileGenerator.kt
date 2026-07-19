package com.albbiz.map.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile covering the app's real cold-start journey: launch,
 * login, land on the map screen, and open/close the hamburger drawer.
 *
 * This exists because a System Trace investigation (see the comments above
 * navigateSafe() in MainActivity.kt, and the MapViewModel.nearMe/topPicks fix) found
 * that several unrelated SDKs (Google Maps' Dynamite module, Firestore's
 * BloomFilter/ExistenceFilter listener-resync machinery) were paying a real,
 * multi-second "verify these classes for the first time" cost the first time their
 * code paths ran, and that cost would land as a main-thread freeze whenever it
 * happened to coincide with a user tap — most visibly the hamburger button. A
 * Baseline Profile tells ART to pre-verify/AOT-compile these exact classes at install
 * time instead of on first use, which is the durable fix for this whole category of
 * jank rather than chasing one triggering interaction at a time.
 *
 * Run via the "Generate Baseline Profile" run configuration in Android Studio, or:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * Requires a connected device or emulator (API 28+, or API 33+ without root).
 *
 * The login step needs a dedicated test/QA account's credentials in
 * android/local.properties (see build.gradle.kts in this module for the exact keys —
 * never hardcode real credentials here, this file is committed to source control).
 * If no credentials are configured, the journey stops after reaching the auth screen,
 * which still profiles app startup/splash but won't cover the map/drawer classes.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        val args = InstrumentationRegistry.getArguments()
        val testEmail = args.getString("benchmarkTestEmail").orEmpty()
        val testPassword = args.getString("benchmarkTestPassword").orEmpty()

        rule.collect(
            packageName = args.getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

            // Start default activity for the app (splash screen).
            pressHome()
            startActivityAndWait()

            if (testEmail.isBlank() || testPassword.isBlank()) {
                // No test account configured — nothing more we can automate past the
                // login wall. Startup/splash is still captured above.
                return@collect
            }

            // Splash auto-navigates once its video ends or an 8s fallback fires, then
            // lands on either "auth" (no session) or "map" (already logged in). Wait
            // for whichever shows up first rather than assuming one or the other,
            // since a prior run on this device may have left a session behind.
            val reachedAuthOrMap = device.wait(
                Until.hasObject(By.res("authEmailField")),
                15_000
            ) || device.hasObject(By.res("mapHamburgerButton"))

            if (!reachedAuthOrMap) return@collect

            // Already logged in from a previous run — skip straight to the drawer
            // journey below.
            if (!device.hasObject(By.res("authEmailField"))) {
                exerciseMapAndDrawer(device)
                return@collect
            }

            val emailField = device.findObject(By.res("authEmailField"))
            emailField.click()
            emailField.setText(testEmail)

            val passwordField = device.findObject(By.res("authPasswordField"))
            passwordField.click()
            passwordField.setText(testPassword)

            device.findObject(By.res("authSubmitButton")).click()

            // Firestore listeners spinning up + MapsInitializer + the map's own
            // "mapReady" loading gate can genuinely take a few seconds the first time,
            // so this timeout is intentionally generous rather than flaky.
            val reachedMap = device.wait(
                Until.hasObject(By.res("mapHamburgerButton")),
                20_000
            )
            if (!reachedMap) return@collect

            exerciseMapAndDrawer(device)
        }
    }

    /**
     * Finds and clicks a tagged element, swallowing StaleObjectException instead of
     * letting it abort the whole run. This is expected, not a bug in the script: the
     * whole point of the rapid-tap burst below is to hit the UI while it's actively
     * changing (a drawer opening, a screen still settling in after a back-navigation),
     * and the accessibility node UiAutomator located a moment ago can legitimately no
     * longer exist by the time the click actually dispatches. A real user's finger
     * doesn't throw an exception when that happens — it just sometimes misses or hits
     * whatever's there now — so we just move on to the next tap in the burst.
     */
    private fun safeClick(device: UiDevice, resId: String) {
        try {
            device.findObject(By.res(resId))?.click()
        } catch (e: StaleObjectException) {
            // Expected during rapid taps on a changing UI — see kdoc above.
        }
    }

    /**
     * The original login->map->drawer journey only ever touched the drawer, never the
     * bottom sheet's content. A later trace (after that first profile was already
     * applied) still showed VerifyClass entries for androidx.compose.ui.node.MyersDiffKt
     * and NodeChain$Differ — Compose's own list-diffing machinery, used by every
     * LazyRow in MapScreen (stories, top recommended, near you, community
     * announcements, most favorited) — because nothing in the recorded journey ever
     * scrolled them. Expanding the bottom sheet and swiping across those carousels
     * here exercises that code path too, so it gets pre-verified along with everything
     * else instead of paying its first-use cost live.
     */
    private fun MacrobenchmarkScope.exerciseMapAndDrawer(device: UiDevice) {
        // Bottom sheet only peeks ~120dp by default — drag it up to reveal the
        // carousels before trying to scroll them.
        device.swipe(
            device.displayWidth / 2, (device.displayHeight * 0.85).toInt(),
            device.displayWidth / 2, (device.displayHeight * 0.35).toInt(),
            15
        )
        device.waitForIdle()
        Thread.sleep(500)

        // Horizontal swipes across the middle of the screen, where the LazyRow
        // carousels sit once the sheet is expanded.
        repeat(3) {
            device.swipe(
                (device.displayWidth * 0.85).toInt(), (device.displayHeight * 0.5).toInt(),
                (device.displayWidth * 0.15).toInt(), (device.displayHeight * 0.5).toInt(),
                10
            )
            device.waitForIdle()
            Thread.sleep(300)
        }

        // Collapse the sheet back down before touching the drawer, so the hamburger
        // button (in the TopAppBar) is reliably visible/tappable again.
        device.swipe(
            device.displayWidth / 2, (device.displayHeight * 0.35).toInt(),
            device.displayWidth / 2, (device.displayHeight * 0.85).toInt(),
            15
        )
        device.waitForIdle()
        Thread.sleep(500)

        // The actual reported trigger, described directly: navigate away to another
        // screen, come back via the back button, and IMMEDIATELY fire off a rapid
        // burst of hamburger taps with NO delay between them. Every earlier version
        // of this script clicked once, waited ~500ms, then clicked again — which
        // never reproduced it, because the app's own debounce/disabled-button guards
        // handle isolated, spaced-out taps just fine. It's specifically the
        // just-navigated-back-and-still-settling window, hit with a genuine unspaced
        // burst, that matters.
        repeat(3) {
            safeClick(device, "mapHamburgerButton")
            device.waitForIdle()
            Thread.sleep(400)

            safeClick(device, "drawerFavoritesItem")
            device.waitForIdle()
            Thread.sleep(1200) // let the destination screen actually finish loading

            device.pressBack()
            // No wait here on purpose — the rapid burst below needs to land in the
            // exact window right after returning to the map, while it's still
            // settling back in, not after everything's idle again.

            repeat(8) {
                safeClick(device, "mapHamburgerButton")
            }

            device.waitForIdle()
            Thread.sleep(800)
            // Get back to a clean, settled map state before the next iteration.
            device.pressBack()
            device.waitForIdle()
            Thread.sleep(500)
        }
    }
}
