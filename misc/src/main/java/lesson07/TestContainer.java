package lesson07;

public class TestContainer {
    @BeforeSuite
    public static void before() {
        System.out.println("Это BEFORE");
    }

//    @BeforeSuite
//    public static void beforeErr() {
//        System.out.println("Это второй BEFORE, тут должно критануть");
//    }

    @AfterSuite
    public static void after() {
        System.out.println("Это AFTER");
    }

//    @AfterSuite
//    public static void afterErr() {
//        System.out.println("Это второй AFTER, тут должно критануть");
//    }

    @Test
    public static void test01() {
        System.out.println("Первый метод без приоритета, должен идти где-то в конце");
    }

    @Test
    public static void test02() {
        System.out.println("Второй метод без приоритета, должен идти где-то в конце");
    }

    @Test(weight = 3)
    public static void test3() {
        System.out.println("Метод с приоритетом 3");
    }

    @Test(weight = 5)
    public static void test5() {
        System.out.println("Метод с приоритетом 5");
    }

    @Test(weight = 10)
    public static void test10() {
        System.out.println("Метод с приоритетом 10");
    }

    @Test(weight = 1)
    public static void test1() {
        System.out.println("Метод с приоритетом 1");
    }
}
