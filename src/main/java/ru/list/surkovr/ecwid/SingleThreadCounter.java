package ru.list.surkovr.ecwid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;

import static ru.list.surkovr.ecwid.App.IP_SPLIT_REGEX;

public class SingleThreadCounter implements Counter {

    private final String sourceFile;

    public SingleThreadCounter(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Long countUniqueIPs() {
        File file = new File(sourceFile);
        BitSet[][][] arr = new BitSet[256][256][256];
        createArray(arr);

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
                    if (!arr[a][b][c].get(d)) {
                        arr[a][b][c].set(d, true);
                        // count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If a small number of duplicates is assumed - it's faster to increment the counter when checking each value (above)
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    for (int l = 0; l < 256; l++) {
                        if (arr[i][j][k].get(l)) count++;
                    }
                }
            }
        }
        return count;
    }

    private void createArray(BitSet[][][] array) {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    array[i][j][k] = new BitSet(256);
                }
            }
        }
    }
}