package dev.beqi.yepblocked

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SpawnLogTest {

    @BeforeEach
    fun resetLog() {
        SpawnLog.reset()
    }

    @Test
    fun `first blocked spawn is reported with the documented format`() {
        val line = SpawnLog.blocked("minecraft:chicken", "SPAWN_ITEM_USE")
        assertEquals("[ENTITY] minecraft:chicken (SPAWN_ITEM_USE) -> BLOCKED", line)
    }

    @Test
    fun `repeated pair is suppressed`() {
        assertNotNull(SpawnLog.blocked("minecraft:chicken", "NATURAL"))
        assertNull(SpawnLog.blocked("minecraft:chicken", "NATURAL"))
        assertNull(SpawnLog.blocked("minecraft:chicken", "NATURAL"))
    }

    @Test
    fun `same entity with another spawn type is reported`() {
        assertNotNull(SpawnLog.blocked("minecraft:zombie", "NATURAL"))
        assertNotNull(SpawnLog.blocked("minecraft:zombie", "SPAWNER"))
        assertNull(SpawnLog.blocked("minecraft:zombie", "NATURAL"))
    }

    @Test
    fun `another entity is reported`() {
        assertNotNull(SpawnLog.blocked("minecraft:zombie", "NATURAL"))
        assertNotNull(SpawnLog.blocked("minecraft:skeleton", "NATURAL"))
    }

    @Test
    fun `reset reports the pair again`() {
        assertNotNull(SpawnLog.blocked("minecraft:chicken", "NATURAL"))
        SpawnLog.reset()
        assertNotNull(SpawnLog.blocked("minecraft:chicken", "NATURAL"))
    }

    @Test
    fun `summary is reported after the threshold`() {
        assertNotNull(SpawnLog.blocked("minecraft:chicken", "NATURAL"))

        var reported = 0
        repeat(999) {
            if (SpawnLog.blocked("minecraft:chicken", "NATURAL") != null) reported++
        }
        assertEquals(0, reported)

        val summary = SpawnLog.blocked("minecraft:chicken", "NATURAL")
        assertNotNull(summary)
        assertEquals(
            "[SUMMARY] blocked 1000 more spawns (total 1000, 1 entity/spawn-type pairs)",
            summary
        )
    }

    @Test
    fun `summary counter continues after the first report`() {
        SpawnLog.blocked("minecraft:bat", "NATURAL")
        repeat(1000) { SpawnLog.blocked("minecraft:bat", "NATURAL") }

        repeat(999) { assertNull(SpawnLog.blocked("minecraft:bat", "NATURAL")) }
        val second = SpawnLog.blocked("minecraft:bat", "NATURAL")
        assertEquals(
            "[SUMMARY] blocked 1000 more spawns (total 2000, 1 entity/spawn-type pairs)",
            second
        )
    }

    @Test
    fun `concurrent blocking reports the pair once`() {
        val threads = 4
        val perThread = 2000
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)

        val futures = (1..threads).map {
            pool.submit<String> {
                start.await()
                val lines = mutableListOf<String>()
                repeat(perThread) {
                    SpawnLog.blocked("minecraft:bat", "NATURAL")?.let(lines::add)
                }
                lines.joinToString("\n")
            }
        }

        start.countDown()
        val lines = futures
            .flatMap { it.get(10, TimeUnit.SECONDS).lines() }
            .filter { it.isNotBlank() }
        pool.shutdown()

        assertEquals(1, lines.count { it.startsWith("[ENTITY]") })
        assertEquals(7, lines.count { it.startsWith("[SUMMARY]") })
    }
}
