object QRegexUtils {
    @JvmStatic
    fun regexExtractFirst(regex: Regex, input: String, removeNewLine: Boolean = true): String {
        if (regex.containsMatchIn(input)) {
            var first = regex.find(input)!!.groups[1]!!.value
            if (removeNewLine) first = first.replace("\\n", "")
            return first.trim()
        }
        return ""
    }

    @JvmStatic
    fun regexExtractFirstNamedGroup(regex: Regex, input: String): Map<String, String> {
        if (regex.containsMatchIn(input)) {
            val firstGroup = regex.find(input)!!.groups as MatchNamedGroupCollection
            return regexExtractAllFirsts(
                "\\?<(.+?)>".toRegex(),
                regex.toString()
            ).associateWith { name -> firstGroup[name]?.value ?: "" }
        }
        return emptyMap()
    }

    @JvmStatic
    fun regexMatchIdxRange(regex: Regex, input: String): IntRange? {
        if (regex.containsMatchIn(input)) {
            return regex.find(input)!!.range
        }
        return null
    }

    @JvmStatic
    fun regexExtractAllFirsts(regex: Regex, input: String, removeNewLine: Boolean = true): List<String> {
        if (regex.containsMatchIn(input)) {
            val matches = regex.findAll(input).toList()
            return matches.map { groups ->
                var first = groups.groupValues[1]
                if (removeNewLine) first = first.replace("\\n", "")
                first.trim()
            }.filter { it != "" }.toList()
        }
        return emptyList()
    }

    @JvmStatic
    fun regexExtractAllNamedGroups(
        regex: Regex,
        input: String
    ): List<Map<String, String>> {
        val all = emptyList<Map<String, String>>().toMutableList()
        if (regex.containsMatchIn(input)) {
            val groupss = regex.findAll(input).map { it.groups as MatchNamedGroupCollection }.toList()
            groupss.forEach { groups ->
                all.add(
                    regexExtractAllFirsts(
                        "\\?<(.+?)>".toRegex(),
                        regex.toString()
                    ).associateWith { name -> groups[name]?.value ?: "" })
            }
            return all
        }
        return emptyList()
    }
}
