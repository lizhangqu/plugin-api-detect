package io.github.lizhangqu.api.detect


import java.util.regex.Pattern


class ApiExtension {
    private List<Pattern> detectPatterns = new ArrayList<>()
    private List<Pattern> detectMethodPatterns = new ArrayList<>()


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


    public void detectMethodPattern(String pattern) {
        if (pattern != null) {
            Pattern p = Pattern.compile(convertToPatternString(pattern))
            if (!detectMethodPatterns.contains(pattern)) {
                detectMethodPatterns.add(p)
            }
        }
    }

    public void detectMethodPatterns(String... patterns) {
        setDetectMethodPatterns(patterns)
    }

    public void setDetectMethodPatterns(String... patterns) {
        if (patterns != null && patterns.length > 0) {
            detectMethodPatterns.removeAll()
            patterns.each {
                Pattern pattern = Pattern.compile(convertToPatternString(it))
                if (!detectMethodPatterns.contains(pattern)) {
                    detectMethodPatterns.add(pattern)
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

    public boolean checkInMethodPattern(String key) {
        if (!detectMethodPatterns.isEmpty()) {
            for (Iterator<Pattern> it = detectMethodPatterns.iterator(); it.hasNext();) {
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
        //convert \\(
        if (input.contains("(")) {
            input = input.replaceAll("\\(", "\\\\(")
        }
        //convert \\)
        if (input.contains(")")) {
            input = input.replaceAll("\\)", "\\\\)")
        }
        //convert \\[
        if (input.contains("[")) {
            input = input.replace("[", "\\[")
        }
        //convert \\]
        if (input.contains("]")) {
            input = input.replace("]", "\\]")
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
