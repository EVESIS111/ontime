package dev.evesis.ontime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Random
import java.util.TimeZone

/**
 * 调度引擎 1000 组随机对拍:A8 的核心。
 * oracle 采用与实现完全异构的"逐分钟暴力步进 + 纯数学本地时间换算",
 * 两者来自不同的思路,一致才有说服力。
 * 时区池刻意只取无夏令时时区(设备在中国,DST 语义需产品决策,不在本引擎范围)。
 */
class ScheduleEngineTest {

    private val tzs = arrayOf(
        TimeZone.getTimeZone("Asia/Shanghai"),
        TimeZone.getTimeZone("UTC"),
        TimeZone.getTimeZone("Asia/Tokyo"))

    private val BASE = 1_770_000_000_000L   // ≈ 2026-02-02

    private fun Rule(t: ScheduleEngine.RepeatType, tod: Int = 0, mask: Int = 0,
                     iv: Int = 60, at: Long = 0, ws: Int = -1, we: Int = -1) =
        ScheduleEngine.Rule(t, tod, mask, iv, at, ws, we)

    @Test
    fun randomCrossCheck1000() {
        val rng = Random(20260814)
        repeat(1000) { i ->
            val type = ScheduleEngine.RepeatType.values()[rng.nextInt(4)]
            val from = BASE + (rng.nextLong() and 0x3FFFFFFFFFFFFL)   // 2026-02 ~ 2028-04
            val rule = when (type) {
                ScheduleEngine.RepeatType.DAILY -> Rule(type, tod = rng.nextInt(1440))
                ScheduleEngine.RepeatType.WEEKLY -> Rule(type, tod = rng.nextInt(1440),
                    mask = rng.nextInt(128))
                ScheduleEngine.RepeatType.INTERVAL -> Rule(type, iv = 1 + rng.nextInt(600))
                ScheduleEngine.RepeatType.ONCE -> Rule(type,
                    at = (from + (rng.nextLong() and 0x3FFFFFFFFL) % (7L * 24 * 3600 * 1000)
                            - 3L * 24 * 3600 * 1000) / 60000L * 60000L)   // ±3~7 天,分钟对齐
            }
            val tz = tzs[rng.nextInt(tzs.size)]
            val horizon = when (type) {
                ScheduleEngine.RepeatType.INTERVAL -> (rule.intervalMinutes * 2L + 90L) * 60_000L
                ScheduleEngine.RepeatType.DAILY -> 48L * 3600_000L
                ScheduleEngine.RepeatType.WEEKLY -> 8L * 24 * 3600_000L
                ScheduleEngine.RepeatType.ONCE -> maxOf(0L, rule.atMillis - from) + 120_000L
            }
            val oracle = bruteNext(rule, from, tz, horizon)
            val got = ScheduleEngine.next(rule, from, tz)
            assertEquals("case#$i rule=$rule from=$from tz=$tz", oracle, got)
        }
    }

    // -------------------------------------------------- 异构 oracle(纯数学)

    private fun localMinuteOfDay(t: Long, tz: TimeZone): Int =
        (((t + tz.getOffset(t)) / 60_000L) % 1440L).toInt()

    /** 1970-01-01 是周四;换算成本地星期索引(0=周日,与 weekMask 位序一致)。 */
    private fun localDayOfWeekIndex(t: Long, tz: TimeZone): Int {
        val localDays = (t + tz.getOffset(t)) / 86_400_000L
        return ((localDays + 4) % 7).toInt()
    }

    private fun bruteNext(rule: ScheduleEngine.Rule, from: Long, tz: TimeZone,
                          horizonMs: Long): Long? {
        var t = (from / 60_000L + 1) * 60_000L
        val end = from + horizonMs
        while (t <= end) {
            val hit = when (rule.type) {
                ScheduleEngine.RepeatType.ONCE -> t == rule.atMillis
                ScheduleEngine.RepeatType.DAILY -> localMinuteOfDay(t, tz) == rule.timeOfDay
                ScheduleEngine.RepeatType.WEEKLY ->
                    localMinuteOfDay(t, tz) == rule.timeOfDay &&
                        (rule.weekMask and (1 shl localDayOfWeekIndex(t, tz))) != 0
                ScheduleEngine.RepeatType.INTERVAL -> (t / 60_000L) % rule.intervalMinutes == 0L
            }
            if (hit) return t
            t += 60_000L
        }
        return null
    }

    // -------------------------------------------------- 固定边界用例

    @Test
    fun sanityCases() {
        val tz = TimeZone.getTimeZone("Asia/Shanghai")
        val today = BASE   // 2026-02-02 05:46:40 UTC = 13:46:40 CST

        // 每日 08:30:今天已过则明天
        val d = Rule(ScheduleEngine.RepeatType.DAILY, tod = 8 * 60 + 30)
        val n1 = ScheduleEngine.next(d, today, tz)!!
        val nextDay = ScheduleEngine.next(d, n1, tz)!!
        assertEquals(24 * 3600_000L, nextDay - n1)

        // 间隔对齐:120 分钟间隔,起点任意,间隔恒为 120 分钟
        val iv = Rule(ScheduleEngine.RepeatType.INTERVAL, iv = 120)
        val a1 = ScheduleEngine.next(iv, today, tz)!!
        val a2 = ScheduleEngine.next(iv, a1, tz)!!
        assertEquals(120 * 60_000L, a2 - a1)
        assertEquals(0L, (a1 / 60_000L) % 120)

        // 每周空掩码 → null
        assertNull(ScheduleEngine.next(
            Rule(ScheduleEngine.RepeatType.WEEKLY, mask = 0), today, tz))

        // 一次性过去时刻 → null
        assertNull(ScheduleEngine.next(
            Rule(ScheduleEngine.RepeatType.ONCE, at = today - 1), today, tz))

        // 一次性未来时刻 → 原样返回
        assertEquals(today + 60_000L,
            ScheduleEngine.next(Rule(ScheduleEngine.RepeatType.ONCE, at = today + 60_000L),
                today, tz))

        // 每周仅周日(mask bit0 = 周日):2026-02-02 是周一,下一次应落在周日(索引 0)
        val sun = Rule(ScheduleEngine.RepeatType.WEEKLY, tod = 9 * 60, mask = 1)
        val n = ScheduleEngine.next(sun, today, tz)!!
        assertEquals(0, localDayOfWeekIndex(n, tz))
    }

    @Test
    fun quietWindowCases() {
        val tz = TimeZone.getTimeZone("Asia/Shanghai")
        // 同日窗口 00:00–08:30 的边界(08:30 整为开区间端点)
        assertTrue(ScheduleEngine.inQuietWindow(0))              // 00:00 起
        assertTrue(ScheduleEngine.inQuietWindow(8 * 60 + 29))    // 08:29 仍在窗口内
        assertFalse(ScheduleEngine.inQuietWindow(8 * 60 + 30))   // 08:30 整,窗口已结束
        assertFalse(ScheduleEngine.inQuietWindow(12 * 60))       // 白天活跃
        assertFalse(ScheduleEngine.inQuietWindow(23 * 60 + 59))  // 深夜活跃(用户作息)
        // quietEnd:03:00 触发 → 顺延到当天 08:30
        val cal = java.util.Calendar.getInstance(tz)
        cal.timeInMillis = BASE
        cal.set(java.util.Calendar.HOUR_OF_DAY, 3)
        cal.set(java.util.Calendar.MINUTE, 0)
        val end = ScheduleEngine.quietEnd(cal.timeInMillis, tz)
        val endCal = java.util.Calendar.getInstance(tz)
        endCal.timeInMillis = end
        assertEquals(8, endCal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(30, endCal.get(java.util.Calendar.MINUTE))
        assertTrue(end - cal.timeInMillis in 1..8 * 3600_000L)
    }

    // -------------------------------------------------- 间隔日窗口(喝水型)

    /** 异构 oracle:日窗口内锚定 windowStart 步进 iv,windowEnd 当日封顶。 */
    private fun windowHit(t: Long, rule: ScheduleEngine.Rule, tz: TimeZone): Boolean {
        val iv = rule.intervalMinutes.toLong()
        val localMin = (t + tz.getOffset(t)) / 60_000L
        val minuteOfDay = localMin % 1440L
        val dayStartMin = localMin - minuteOfDay
        val mFromStart = localMin - (dayStartMin + rule.windowStart)
        return mFromStart >= 0 && mFromStart % iv == 0L && minuteOfDay <= rule.windowEnd
    }

    @Test
    fun windowedIntervalCases() {
        val tz = TimeZone.getTimeZone("Asia/Shanghai")
        // 喝水规则:每 45 分钟,09:00-21:30
        val w = Rule(ScheduleEngine.RepeatType.INTERVAL, iv = 45, ws = 9 * 60, we = 21 * 60 + 30)
        fun at(h: Int, m: Int, dayOffset: Int = 0): Long {
            val c = java.util.Calendar.getInstance(tz)
            c.timeInMillis = BASE   // 2026-02-02 13:46:40 CST(周一)
            c.add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
            c.set(java.util.Calendar.HOUR_OF_DAY, h)
            c.set(java.util.Calendar.MINUTE, m)
            c.set(java.util.Calendar.SECOND, 0)
            return c.timeInMillis
        }
        assertEquals(at(9, 0), ScheduleEngine.next(w, at(8, 0), tz))
        assertEquals(at(9, 45), ScheduleEngine.next(w, at(9, 0), tz))          // 严格大于
        assertEquals(at(18, 0), ScheduleEngine.next(w, at(17, 20), tz))        // 网格 17:15 已过
        assertEquals(at(21, 0), ScheduleEngine.next(w, at(20, 59), tz))        // 当日最后一响
        assertEquals(at(9, 0, 1), ScheduleEngine.next(w, at(21, 0), tz))       // 下一格 21:45 越窗 → 次日
        assertEquals(at(9, 0, 1), ScheduleEngine.next(w, at(21, 14), tz))      // 尾巴内无格点 → 次日
        assertEquals(at(9, 0, 1), ScheduleEngine.next(w, at(21, 30), tz))
        assertEquals(at(9, 0, 1), ScheduleEngine.next(w, at(23, 0), tz))
        // 连续触发节律:全链步进应保持 45 分钟,并在窗口尾正确跳日
        var t = ScheduleEngine.next(w, at(8, 0), tz)!!
        var fires = 0
        while (fires < 40) {
            val t2 = ScheduleEngine.next(w, t, tz)!!
            if (t2 - t != 45 * 60_000L) {
                // 唯一允许的例外:跨窗跳日
                org.junit.Assert.assertTrue("跳日外的间隔异常: ${t2 - t}", t2 - t >= 11 * 3600_000L)
            }
            t = t2; fires++
        }
        assertEquals(40, fires)
    }

    @Test
    fun windowedIntervalRandom200() {
        val rng = Random(20260816)
        val tz = tzs[rng.nextInt(tzs.size)]
        repeat(200) { i ->
            val ws = rng.nextInt(1200)
            val we = ws + 1 + rng.nextInt(1439 - ws + 0).coerceAtLeast(1).coerceAtMost(1439 - ws)
            val rule = Rule(ScheduleEngine.RepeatType.INTERVAL,
                iv = 1 + rng.nextInt(300), ws = ws, we = we)
            val from = BASE + (rng.nextLong() and 0x3FFFFFFFFL)
            val horizon = 26L * 3600_000L + rule.intervalMinutes * 2L * 60_000L
            var t = (from / 60_000L + 1) * 60_000L
            var oracle: Long? = null
            val endT = from + horizon
            while (t <= endT) {
                if (windowHit(t, rule, tz)) { oracle = t; break }
                t += 60_000L
            }
            assertEquals("case#$i rule=$rule from=$from tz=$tz",
                oracle, ScheduleEngine.next(rule, from, tz))
        }
    }
}

private fun assertFalse(b: Boolean) = org.junit.Assert.assertFalse(b)
private fun assertTrue(b: Boolean) = org.junit.Assert.assertTrue(b)

class PickMessageTest {
    private fun r(msg: String) = Reminder(1, "t", msg, null, "", "coin",
        ScheduleEngine.RepeatType.DAILY, 0, 0, 60, 0, 10, true, 0, 0)

    @Test
    fun variants() {
        // 单句:始终原样
        assertEquals("只有一句", r("只有一句").pickMessage())
        // 多句:1000 次抽样只落在变体集合内,且每个都被选中过(随机性可用)
        val set = setOf("甲台词", "乙台词", "丙台词")
        val msg = set.joinToString("|")
        val hits = mutableSetOf<String>()
        repeat(1000) { hits.add(r(msg).pickMessage()) }
        assertEquals(set, hits)
        // 空白变体容错:过滤空段后仍在有效集合内
        val hit2 = mutableSetOf<String>()
        repeat(100) { hit2.add(r("A || | B").pickMessage()) }
        assertEquals(setOf("A", "B"), hit2)
    }
}
