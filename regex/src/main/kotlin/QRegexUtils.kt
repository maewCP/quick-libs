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
    fun regExExtractFirstNamedGroup(regex: Regex, input: String): Map<String, String> {
        if (regex.containsMatchIn(input)) {
            val firstGroup = regex.find(input)!!.groups as MatchNamedGroupCollection
            return regExExtractAllFirsts(
                "\\?<(.+?)>".toRegex(),
                regex.toString()
            ).associateWith { name -> firstGroup[name]?.value ?: "" }
        }
        return emptyMap()
    }

    @JvmStatic
    fun regExMatchIdxRange(regex: Regex, input: String): IntRange? {
        if (regex.containsMatchIn(input)) {
            return regex.find(input)!!.range
        }
        return null
    }

    @JvmStatic
    fun regExExtractAllFirsts(regex: Regex, input: String, removeNewLine: Boolean = true): List<String> {
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
    fun regExExtractAllNamedGroups(
        regex: Regex,
        input: String
    ): List<Map<String, String>> {
        val all = emptyList<Map<String, String>>().toMutableList()
        if (regex.containsMatchIn(input)) {
            val groupss = regex.findAll(input).map { it.groups as MatchNamedGroupCollection }.toList()
            groupss.forEach { groups ->
                all.add(
                    regExExtractAllFirsts(
                        "\\?<(.+?)>".toRegex(),
                        regex.toString()
                    ).associateWith { name -> groups[name]?.value ?: "" })
            }
            return all
        }
        return emptyList()
    }
}
