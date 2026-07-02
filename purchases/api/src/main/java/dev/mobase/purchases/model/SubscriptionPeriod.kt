package dev.mobase.purchases.model

data class SubscriptionPeriod(val value: Int, val unit: Unit) {
    enum class Unit { DAY, MONTH, WEEK, YEAR }

    companion object {
        private val PATTERN = Regex("""^P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)W)?(?:(\d+)D)?$""")

        fun parse(period: String): SubscriptionPeriod {
            val match = PATTERN.matchEntire(period)
                ?: throw IllegalArgumentException("Invalid period: $period")

            val (years, months, weeks, days) = match.destructured

            return when {
                years.isNotEmpty() -> SubscriptionPeriod(years.toInt(), Unit.YEAR)
                months.isNotEmpty() -> SubscriptionPeriod(months.toInt(), Unit.MONTH)
                weeks.isNotEmpty() -> SubscriptionPeriod(weeks.toInt(), Unit.WEEK)
                days.isNotEmpty() -> SubscriptionPeriod(days.toInt(), Unit.DAY)
                else -> throw IllegalStateException("Invalid period: $period")
            }
        }
    }
}