package at.xirado.jdui.utils

fun String.splitIntoParts(n: Int, maxSize: Int): List<String> {
    if (length < n || length > n * maxSize) {
        throw IllegalArgumentException("The string is too short to split into $n parts or too long for each part to fit within the max size of $maxSize")
    }

    val result = mutableListOf<String>()
    val codePoints = this.codePoints().toArray() // Convert to array of Unicode code points
    val totalLength = codePoints.size
    val chunkSize = totalLength / n
    val remainder = totalLength % n
    var start = 0

    for (i in 0 until n) {
        val end = start + chunkSize + if (i < remainder) 1 else 0
        if (end - start > maxSize) {
            throw IllegalArgumentException("Chunk size exceeded maxSize of $maxSize")
        }

        // Convert code points back to string
        result.add(String(codePoints, start, end - start))
        start = end
    }

    return result
}