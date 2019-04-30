package io.github.lizhangqu.api.detect


import java.util.regex.Pattern


class ApiExtension {
    private List<Pattern> detectPatterns = new ArrayList<>()

    public List<Pattern> getDetectPatterns() {
        return blockPatterns
    }

    public void detectPattern(String pattern) {
        if (pattern != null) {
            Pattern p = Pattern.compile(convertToPatternString(pattern))
            if (!detectPatterns.contains(pattern)) {
                detectPatterns.add(p)
            }
        }
    }

    public void detectPatterns(String... patterns) {
        setDetectPatterns(patterns)
    }

    public void setDetectPatterns(String... patterns) {
        if (patterns != null && patterns.length > 0) {
            detectPatterns.removeAll()
            patterns.each {
                Pattern pattern = Pattern.compile(convertToPatternString(it))
                if (!detectPatterns.contains(pattern)) {
                    detectPatterns.add(pattern)
                }
            }
        }
    }

    public boolean checkInPattern(String key) {
        if (!detectPatterns.isEmpty()) {
            for (Iterator<Pattern> it = detectPatterns.iterator(); it.hasNext();) {
                Pattern p = it.next()
                if (p.matcher(key).matches()) {
                    return true
                }
            }
        }
        return false
    }


    private static String convertToPatternString(String input) {
        //convert \\.
        if (input.contains(".")) {
            input = input.replaceAll("\\.", "\\\\.")
        }
        //convert ？to .
        if (input.contains("?")) {
            input = input.replaceAll("\\?", "\\.")
        }
        //convert * to.*
        if (input.contains("*")) {
            input = input.replace("*", ".*")
        }
        return input;
    }


}
