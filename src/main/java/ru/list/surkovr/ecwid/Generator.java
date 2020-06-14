package ru.list.surkovr.ecwid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class Generator {

    private static final long STRING_COUNT = 5_000L;

    private final String sourceFile;

    public Generator(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void generateFile() {
        long startGen = System.currentTimeMillis();
        String dir = sourceFile.replaceAll(
                sourceFile.split("/")[sourceFile.split("/").length - 1], "");
        File file;
        try {
            if (Files.notExists(Path.of(dir))) Files.createDirectories(Path.of(dir));
            Files.deleteIfExists(Path.of(sourceFile));
            Files.createFile(Path.of(sourceFile));
            file = new File(sourceFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fw)) {
            for (long i = 0; i < STRING_COUNT; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(new Random().nextInt(255)).append(".")
                        .append(new Random().nextInt(255)).append(".")
                        .append(new Random().nextInt(255)).append(".")
                        .append(new Random().nextInt(255)).append("\n");
                bufferedWriter.write(sb.toString());
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File has generated for " + (System.currentTimeMillis() - startGen) + " ms");
    }
}