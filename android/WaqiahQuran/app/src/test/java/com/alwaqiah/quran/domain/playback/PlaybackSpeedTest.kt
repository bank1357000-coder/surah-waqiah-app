package com.alwaqiah.quran.domain.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSpeedTest {
    @Test fun stepsUp() = assertEquals(1.25f, PlaybackSpeed.step(1.0f, 0.25f, 1), 0.001f)
    @Test fun stepsDown() = assertEquals(1.0f, PlaybackSpeed.step(1.25f, 0.25f, -1), 0.001f)
    @Test fun snapsOffGridUp() = assertEquals(1.25f, PlaybackSpeed.step(1.1f, 0.25f, 1), 0.001f)
    @Test fun snapsOffGridDown() = assertEquals(1.0f, PlaybackSpeed.step(1.1f, 0.25f, -1), 0.001f)
    @Test fun fineStep() = assertEquals(1.05f, PlaybackSpeed.step(1.0f, 0.05f, 1), 0.001f)
    @Test fun clampsAtMax() = assertEquals(2.0f, PlaybackSpeed.step(2.0f, 0.1f, 1), 0.001f)
    @Test fun clampsAtMin() = assertEquals(1.0f, PlaybackSpeed.step(1.0f, 0.1f, -1), 0.001f)
    @Test fun clampRounds() = assertEquals(1.33f, PlaybackSpeed.clamp(1.3333f), 0.001f)
}
