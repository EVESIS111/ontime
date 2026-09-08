package dev.evesis.ontime.ui.editor

import dev.evesis.ontime.ScheduleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditorValidationTest {
    private val now = 1_800_000_000_000L
    private val valid = EditorUiState(title = "提醒")

    @Test fun blankTitleHasActionableError() {
        assertEquals("请填写提醒标题", valid.copy(title = "  ").validationError(now))
    }

    @Test fun weeklyNeedsAtLeastOneDay() {
        val weekly = valid.copy(repeatType = ScheduleEngine.RepeatType.WEEKLY, weekMask = 0)
        assertEquals("请至少选择一天", weekly.validationError(now))
        assertNull(weekly.copy(weekMask = 1).validationError(now))
    }

    @Test fun onceRejectsPastAndCurrentTime() {
        val once = valid.copy(repeatType = ScheduleEngine.RepeatType.ONCE)
        assertEquals("请选择未来的提醒时间", once.copy(atMillis = now - 1).validationError(now))
        assertEquals("请选择未来的提醒时间", once.copy(atMillis = now).validationError(now))
        assertNull(once.copy(atMillis = now + 1).validationError(now))
    }

    @Test fun intervalRejectsZeroWithoutAffectingOtherTypes() {
        assertEquals("提醒间隔必须大于零", valid.copy(
            repeatType = ScheduleEngine.RepeatType.INTERVAL, intervalMinutes = 0).validationError(now))
        assertNull(valid.copy(intervalMinutes = 0).validationError(now))
    }
    @Test fun intervalWindowCannotSilentlyBecomeAllDay() {
        val interval = valid.copy(repeatType = ScheduleEngine.RepeatType.INTERVAL, windowStart = 540)
        assertEquals("结束时间必须晚于开始时间", interval.copy(windowEnd = 540).validationError(now))
        assertEquals("结束时间必须晚于开始时间", interval.copy(windowEnd = 480).validationError(now))
        assertNull(interval.copy(windowEnd = 1290).validationError(now))
        assertNull(interval.copy(windowStart = -1, windowEnd = -1).validationError(now))
    }
}
