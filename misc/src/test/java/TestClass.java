import lesson06.Lesson06;
import org.junit.Assert;
import org.junit.Test;

public class TestClass {
    @Test
    public void splitTest1() {
        Assert.assertArrayEquals(new int[]{1, 7}, Lesson06.arrayExcerpt(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7}));
    }

    @Test
    public void splitTest2() {
        Assert.assertEquals(0, Lesson06.arrayExcerpt(new int[]{4, 4, 4, 4}).length);
    }

    @Test(expected = RuntimeException.class)
    public void splitTest3() {
        Lesson06.arrayExcerpt(new int[]{1, 2, 3, 5, 2, 3, 2, 1, 7});
    }

    @Test
    public void splitTest4() {
        long start = System.currentTimeMillis();
        Lesson06.arrayExcerpt(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7});
        long finish = System.currentTimeMillis();
        if (finish - start > 100) {
            Assert.fail();
        }
    }

    @Test
    public void checkTest1() {
        Assert.assertFalse(Lesson06.arrayContentCheck(new int[]{4, 4, 4, 4}));
    }

    @Test
    public void checkTest2() {
        Assert.assertFalse(Lesson06.arrayContentCheck(new int[]{1, 1, 1, 1}));
    }

    @Test
    public void checkTest3() {
        Assert.assertTrue(Lesson06.arrayContentCheck(new int[]{4, 1, 4, 4}));
    }

    @Test
    public void checkTest4() {
        Assert.assertFalse(Lesson06.arrayContentCheck(new int[]{4, 3, 4, 4}));
    }
}
