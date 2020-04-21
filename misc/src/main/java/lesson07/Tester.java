package lesson07;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public  class Tester {
    private static Method before = null;
    private static Method after = null;
    private static List<Method> testMethods = new ArrayList<>();
    private static Object obj = null;

    public static void start(Class in) throws Exception {

        // Раскладываем методы по местам
        for (Method method : in.getDeclaredMethods()) {
            parseMethod(method);
        }

        // Сортируем массив тестов по весу
        Collections.sort(testMethods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o2.getDeclaredAnnotation(Test.class).weight() -  o1.getDeclaredAnnotation(Test.class).weight();
            }
        });

        // Запускаем тесты...
        runTests(in);
    }

    private static void parseMethod(Method in) {
        // Проверка на BeforeSuite
        if (in.isAnnotationPresent(BeforeSuite.class)) {
            if (before == null) {
                before = in;
            } else {
                throw new RuntimeException("Может быть только один класс, помеченный как BeforeSuite!");
            }
        }

        // Проверка на AfterSuite
        if (in.isAnnotationPresent(AfterSuite.class)) {
            if (after == null) {
                after = in;
            } else {
                throw new RuntimeException("Может быть только один класс, помеченный как AfterSuite!");
            }
        }

        // Проверка на наличие аннотации Test
        if (in.isAnnotationPresent(Test.class)) {
            testMethods.add(in);
        }
    }

    private static void runTests(Class in) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // Запускаем BeforeSuite, если есть
        if (before != null) {
            if (Modifier.isStatic(before.getModifiers())) {
                before.invoke(in);
            } else {
                if (obj == null) {
                    obj = in.getDeclaredConstructor(in).newInstance();
                }
                before.invoke(obj);
            }
        }

        // Запускаем сами тесты, если они есть
        if (testMethods.size() > 0) {
            for (Method m : testMethods) {
                if (Modifier.isStatic(m.getModifiers())) {
                    m.invoke(in);
                } else {
                    if (obj == null) {
                        obj = in.getDeclaredConstructor(in).newInstance();
                    }
                    m.invoke(obj);
                }
            }
        }

        // Запускаем AfterSuite, если есть
        if (after != null) {
            if (Modifier.isStatic(after.getModifiers())) {
                after.invoke(in);
            } else {
                if (obj == null) {
                    obj = in.getDeclaredConstructor(in).newInstance();
                }
                after.invoke(obj);
            }
        }
    }
}
