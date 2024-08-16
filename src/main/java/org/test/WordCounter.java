package org.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class WordCounter {

    public static long countWordsInFile(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.flatMap(line -> Stream.of(line.split("\\s+")))
                    .filter(word -> !word.isEmpty())
                    .count();
        }
    }

    public static void countWordsInFile(Path filePath, Map<String, Long> wordCounts) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.flatMap(line -> Stream.of(line.split("\\s+")))
                    .map(String::toLowerCase)
                    .filter(word -> !word.isEmpty())
                    .forEach(word -> wordCounts.merge(word, 1L, Long::sum));
        }
    }
}
