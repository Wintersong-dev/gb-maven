import lesson06.Lesson06;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] example1 = {1, 2, 4, 4, 2, 3, 4, 1, 7};
        int[] example2 = {4, 4, 4, 4};
        int[] example3 = {1, 2, 3, 5, 2, 3, 2, 1, 7};

        System.out.println(Arrays.toString(Lesson06.arrayExcerpt(example1)));
        System.out.println(Arrays.toString(Lesson06.arrayExcerpt(example2)));
        System.out.println(Arrays.toString(Lesson06.arrayExcerpt(example3)));
    }



}
