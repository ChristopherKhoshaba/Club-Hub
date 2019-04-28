package codingnojutsu.clubhub

data class Spot(
    val id: Long = counter++,
    val name: String,
    val type: String,
    val url: String
) {
    companion object {
        private var counter = 0L
    }
}
