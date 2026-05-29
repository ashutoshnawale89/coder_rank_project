package com.code.rank.executor;

import com.code.rank.entity.Language;

import java.util.EnumMap;
import java.util.Map;

public final class LanguageSpec {

    public final String image;
    public final String sourceFile;
    public final String compileCommand;
    public final String runCommand;

    private LanguageSpec(String image, String sourceFile, String compileCommand, String runCommand) {
        this.image = image;
        this.sourceFile = sourceFile;
        this.compileCommand = compileCommand;
        this.runCommand = runCommand;
    }

    private static final Map<Language, LanguageSpec> SPECS = new EnumMap<>(Language.class);
    static {
        SPECS.put(Language.PYTHON, new LanguageSpec(
                "python:3.11-slim",
                "main.py",
                null,
                "python /sandbox/main.py"));
        SPECS.put(Language.JAVA, new LanguageSpec(
                "eclipse-temurin:17-jdk-jammy",
                "Main.java",
                "javac /sandbox/Main.java -d /tmp",
                "java -cp /tmp Main"));
        SPECS.put(Language.JAVASCRIPT, new LanguageSpec(
                "node:18-alpine",
                "main.js",
                null,
                "node /sandbox/main.js"));
    }

    public static LanguageSpec of(Language language) {
        LanguageSpec spec = SPECS.get(language);
        if (spec == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return spec;
    }
}
