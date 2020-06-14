package ru.list.surkovr.ecwid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static ru.list.surkovr.ecwid.App.IP_SPLIT_REGEX;

public class SingleThreadCounter implements Counter {

    private final String sourceFile;

    public SingleThreadCounter(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Long countUniqueIPs() {
        File file = new File(sourceFile);
        final boolean[][][][] uniqueIPs = new boolean[256][256][256][256];

        long count = 0L;
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            String s;
            while ((s = br.readLine()) != null) {
                try {
                    String[] splitted = s.split(IP_SPLIT_REGEX);
                    if (splitted.length != 4) continue;
                    int a = Integer.parseInt(splitted[0]);
                    int b = Integer.parseInt(splitted[1]);
                    int c = Integer.parseInt(splitted[2]);
                    int d = Integer.parseInt(splitted[3]);
                    if (!uniqueIPs[a][b][c][d]) {
                        uniqueIPs[a][b][c][d] = true;
                        // count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Если подразумевается малое количество дубликатов - быстрее счетчик увеличивать при проверке каждого значения (выше)
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    for (int l = 0; l < 256; l++) {
                        if (uniqueIPs[i][j][k][l]) count++;
                    }
                }
            }
        }
        return count;
    }
}