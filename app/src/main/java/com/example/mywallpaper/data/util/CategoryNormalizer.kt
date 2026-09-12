package com.example.mywallpaper.data.util

object CategoryNormalizer {

    private val ALIAS_MAP = mapOf(
        "ainme" to "anime",
        "hindusim" to "hinduism",
        "cats_and_dogs" to "kitty",
        "cat" to "kitty",
        "dog" to "kitty"
    )

    /**
     * Normalizes any category string by trimming, lowercasing, and resolving known aliases/typos.
     */
    fun normalize(category: String): String {
        val trimmed = category.trim().lowercase()
        return ALIAS_MAP[trimmed] ?: trimmed
    }

    /**
     * Formats category for friendly UI display (e.g. "anime" -> "Anime").
     */
    fun toDisplayName(category: String): String {
        val normalized = normalize(category)
        return when (normalized) {
            "anime" -> "Anime"
            "cars" -> "Cars"
            "nature" -> "Nature"
            "islamic" -> "Islamic"
            "hinduism" -> "Hinduism"
            "kitty" -> "Pets & Animals"
            "abstract" -> "Abstract"
            "architecture" -> "Architecture"
            "space" -> "Space"
            "gaming" -> "Gaming"
            "cyberpunk" -> "Cyberpunk"
            "minimal" -> "Minimal"
            "dark" -> "Dark"
            "animals" -> "Animals"
            "city" -> "City"
            "sports" -> "Sports"
            "fantasy" -> "Fantasy"
            "technology" -> "Technology"
            else -> normalized.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    /**
     * Checks if a wallpaper's category matches a target category, accounting for aliases and case-insensitivity.
     * An empty or "all" target matches everything.
     */
    fun matches(wallpaperCategory: String, targetCategory: String): Boolean {
        if (targetCategory.isBlank() || targetCategory.equals("all", ignoreCase = true)) {
            return true
        }
        return normalize(wallpaperCategory) == normalize(targetCategory)
    }

    /**
     * Extracts all unique normalized categories from a list of wallpapers.
     * Only categories with at least one wallpaper are included.
     */
    fun extractCategories(categories: Collection<String>): List<String> {
        return categories
            .map { normalize(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}
