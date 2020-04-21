package lesson07;

public class Main {
    public static void main(String[] args) {
        try {
            Tester.start(TestContainer.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
