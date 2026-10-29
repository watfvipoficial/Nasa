package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioEngine
import com.example.campaign.CampaignData
import com.example.game.GameManager
import com.example.haptics.HapticController
import com.example.haptics.HapticPattern
import com.example.model.PowerSystem
import com.example.model.ShipCustomization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AstroStrike", appName)
  }

  @Test
  fun `test haptic controller triggers without crashing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val controller = HapticController(context)
    HapticPattern.entries.forEach { pattern ->
      controller.trigger(pattern)
    }
    assertTrue(true)
  }

  @Test
  fun `test game manager mission lifecycle and audio reset`() {
    val audioEngine = AudioEngine()
    var lastTriggeredPattern: HapticPattern? = null
    val gameManager = GameManager(
      audioEngine = audioEngine,
      onVibratePattern = { lastTriggeredPattern = it }
    )

    val sector = CampaignData.initialSectors[0]
    val customization = ShipCustomization()
    gameManager.startMission(sector, customization)

    assertEquals(sector.id, gameManager.currentSector.id)
    assertEquals(0.2f, audioEngine.combatIntensity, 0.01f)

    // Divert power
    gameManager.divertPower(PowerSystem.WEAPONS_FOCUS)
    assertEquals(PowerSystem.WEAPONS_FOCUS, gameManager.powerSystem)

    // Trigger haptic
    gameManager.onVibratePattern?.invoke(HapticPattern.UI_TAP)
    assertEquals(HapticPattern.UI_TAP, lastTriggeredPattern)

    // End mission resets audio intensity
    gameManager.endMission()
    assertEquals(0f, audioEngine.combatIntensity, 0.001f)
  }
}
