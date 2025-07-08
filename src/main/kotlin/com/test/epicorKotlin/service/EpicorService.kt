package com.test.epicorKotlin.service


import com.test.epicorKotlin.model.Response
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern

@Service
class EpicorService {

    private val logger: Logger = LoggerFactory.getLogger(EpicorService::class.java)

    companion object {
        private val REMOVE_WORDS: CharArraySet = EnglishAnalyzer.getDefaultStopSet()
        private val CLEAN_PATTERN: Pattern = Pattern.compile("[^a-zA-Z\\s']")

        private val proNounsSet: Set<String> = setOf(
            "he", "she", "it", "its", "they", "them", "his", "her", "their", "all",
            "we", "us", "you", "i", "me", "my", "mine", "our", "ours", "your", "yours", "him", "hers", "one", "which", "some", "what"
        )

        private val conjunctionSet: Set<String> = setOf(
            "so", "yet", "although", "because", "since", "unless", "until", "while",
            "after", "before", "once", "though", "when", "whenever", "where",
            "wherever", "whereas", "than", "whether", "either", "neither", "both"
        )

        private val prePositionSet: Set<String> = setOf(
            "from", "about", "above", "across", "against", "along", "among", "around", "before",
            "behind", "below", "beneath", "beside", "between", "beyond", "during", "inside",
            "near", "off", "out", "over", "through", "under", "until", "upon", "within", "without", "like", "up"
        )

        private val verbsSet: Set<String> = setOf(
            "is", "am", "are", "was", "were", "be", "being", "been",

            // Forms of "have"
            "have", "has", "had",

            // Forms of "do"
            "do", "does", "did",

            // Modal verbs
            "can", "could", "may", "might", "must",
            "shall", "should", "will", "would"
        )
    }


    fun fileRead(furl: String?): Response {
        val url= furl?.takeIf { it.isNotBlank() }
            ?: "https://courses.cs.washington.edu/courses/cse390c/22sp/lectures/moby.txt"
        logger.info("Fetching text from URL: {}", url)

        val countList = getWords(url)
        logger.info("countList without Filters: {}", countList.size)

        logger.info("Going to find top 5 words...")

        val countMap = countList.asSequence()
            .map { if (it.startsWith("'")) it.substring(1) else it }
            .filter { it.isNotBlank() && it !in REMOVE_WORDS && it !in proNounsSet && it !in conjunctionSet && it !in prePositionSet && it !in verbsSet }
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toLong() }

        val top5WordsMap = countMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .associate { it.key to it.value }

        logger.info("top 5 words: $top5WordsMap")

        val top50WordsSet = countMap.keys
            .toSortedSet()
            .take(50)
            .toSortedSet()

        logger.info("top 50 unique records after exclusions: $top50WordsSet")

        return Response(
            count = countList.size,
            top5Words = top5WordsMap,
            top50Words = top50WordsSet
        )
    }

    private fun getWords(furl: String): List<String> {
        val wordList = mutableListOf<String>()
        BufferedReader(InputStreamReader(URL(furl).openStream())).use { reader ->
            reader.lines().forEach { line ->
                line.lowercase(Locale.getDefault())
                    .replace("'", "")
                    .let { CLEAN_PATTERN.matcher(it).replaceAll(" ") }
                    .split("\\s+".toRegex()) // Split into tokens
                    .map { word -> if (word.endsWith("'s")) word.dropLast(2) else word }
                    .filter { it.isNotBlank() }
                    .forEach { wordList.add(it) }
            }
        }
        return wordList
    }
}



