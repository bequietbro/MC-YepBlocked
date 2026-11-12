package dev.beqi.yepblocked

object SpawnLog {

    private const val SUMMARY_EVERY = 1000

    private val reported = HashSet<String>()
    private var suppressedSinceSummary = 0
    private var totalSuppressed = 0

    @Synchronized
    fun reset() {
        reported.clear()
        suppressedSinceSummary = 0
        totalSuppressed = 0
    }

    @Synchronized
    fun blocked(entityId: String, spawnType: String): String? {
        if (reported.add("$entityId|$spawnType")) {
            return "[ENTITY] $entityId ($spawnType) -> BLOCKED"
        }

        suppressedSinceSummary++
        totalSuppressed++

        if (suppressedSinceSummary < SUMMARY_EVERY) return null

        val count = suppressedSinceSummary
        suppressedSinceSummary = 0
        return "[SUMMARY] blocked $count more spawns (total $totalSuppressed, ${reported.size} entity/spawn-type pairs)"
    }
}
