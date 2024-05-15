package com.phunghv.god.toogle_text;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringConversionFactory {
    private static final char[] MARK_CHARACTERS = {'_', '-'};

    private static final Pattern p = Pattern.compile("^\\W+");

    private static List<String> parseWords(String source) {
        for (var character : MARK_CHARACTERS) {
            source = source.replace(character, ' ');
        }
        if (source.contains(" ")) {
            source = source.replaceAll("\\s+", " ");
            return Arrays.stream(source.split(" ")).map(String::toLowerCase).toList();
        }
        if (StringUtils.isAllUpperCase(source) || StringUtils.isAllLowerCase(source)) {
            return List.of(source.toLowerCase());
        }
        source = splitByUppercase(StringUtils.uncapitalize(source));
        return Arrays.stream(source.split(" ")).map(String::toLowerCase).toList();
    }

    private static String splitByUppercase(String source) {
        var builder = new StringBuilder();
        for (var i = 0; i < source.length(); i++) {
            if (Character.isUpperCase(source.charAt(i))) {
                builder.append(" ");
            }
            builder.append(source.charAt(i));
        }
        return builder.toString();
    }

    public static String getNext(String source) {
        if (StringUtils.isBlank(source)) {
            return source;
        }
        var converted = convert(source);
        var index = converted.indexOf(source);
        if (index < 0) {
            return converted.get(0);
        }
        var length = converted.size();
        index = (index + 1) % length;
        return converted.get(index);
    }

    public static List<String> convert(String source) {
        Collection<String> convertedTexts = new LinkedHashSet<>();
        var appendText = "";
        Matcher m = p.matcher(source);
        if (m.find()) {
            appendText = m.group(0);
        }
        //remove all special chars
        source = source.replaceAll("^\\W+", "");
        var words = parseWords(source);
        for (var character : MARK_CHARACTERS) {
            var converted = convertSnakeCase(words, character);
            convertedTexts.add(converted);
            convertedTexts.add(converted.toUpperCase());
        }
        var camelCase = convertCamelCase(words);
        convertedTexts.add(camelCase);
        convertedTexts.add(StringUtils.uncapitalize(camelCase));
        final var prefix = appendText;
        return convertedTexts.stream().map(i -> prefix + i).collect(Collectors.toList());
    }

    private static String convertSnakeCase(List<String> words, char separator) {
        return String.join(separator + "", words);
    }

    private static String convertCamelCase(List<String> words) {
        var camelCased = new StringBuilder();
        for (String token : words) {
            if (!token.isEmpty()) {
                camelCased.append(token.substring(0, 1).toUpperCase()).append(token.substring(1));
            } else {
                camelCased.append("_");
            }
        }
        return camelCased.toString();
    }

    public static String convertSnakeCase(String source) {
        var words = parseWords(source);
        return convertSnakeCase(words, '_');
    }

    public static String convertCamelCase(String source) {
        var words = parseWords(source);
        var text = convertCamelCase(words);
        return StringUtils.uncapitalize(text);
    }
}
