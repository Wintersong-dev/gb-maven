package lesson06;

import java.util.Arrays;

public class Lesson06 {

    // Задание урока 6, часть 1
    public static int[] arrayExcerpt(int[] in) {
        int[] out = new int[1];
        boolean has4 = false;

        for (int i = in.length - 1; i > 0; i--) {
            if (in[i] == 4) {
                return Arrays.copyOfRange(in, i + 1, in.length);
            }
        }
        throw new RuntimeException("В массиве нет четверок!");
    }

    public static boolean arrayContentCheck(int[] in) {
        boolean has1 = false;
        boolean has4 = false;
        for (int i = 0; i < in.length - 1; i++) {
            if (in[i] != 1 && in[i] != 4) {
                return false;
            }
            has1 = in[i] == 1 ? true : has1;
            has4 = in[i] == 4 ? true : has4;
        }
        return has1 && has4;
    }
}
