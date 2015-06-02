package cc.koosha.annotation;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class AnnotationBuilderImplTest {
   private @interface SomeInterface {
      String v0() default "v0";
      int v1();
      int v2() default 2;
   }

   private static SomeInterface getFrom(final Object o) {
      return (SomeInterface) o;
   }

   private AnnotationBuilder<SomeInterface> builder = null;

   @Before
   public void setUp() throws Exception {
      this.builder = AnnotationBuilderImpl.of(SomeInterface.class);
   }

   @Test
   public void testOf() throws Exception {
      Assert.assertNotNull("Builder returned null", this.builder);
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void fewArgs() throws Exception {
      this.builder.get("a", 1, 2, 3);
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void tooManyArgs() throws Exception {
      this.builder.get("a", 1);
   }

   @Test
   public void defaultValues() throws Exception {
      final SomeInterface si = getFrom(this.builder.get(null, 99, null));

      final String msg0 = "Runtime implementation had different value than expected";
      final String msg1 = "Runtime implementation used default vaule, while a value was provided explicitly";

      Assert.assertEquals(msg1, "v0", si.v0());
      Assert.assertEquals(msg0, 99, si.v1());
      Assert.assertEquals(msg1, 2, si.v2());
   }

   @Test(expected = NullPointerException.class)
   public void nullOnNonDefault() throws Exception {
      this.builder.get(null, null, null);
   }

   @Test
   public void nonDefaultValues() throws Exception {
      final String v0 = "hello";
      final int v1 = 777;
      final int v2 = 888;

      final SomeInterface si = getFrom(this.builder.get(v0, v1, v2));
      final String msg = "Runtime implementation had different value than expected";
      Assert.assertEquals(msg, v0, si.v0());
      Assert.assertEquals(msg, v1, si.v1());
      Assert.assertEquals(msg, v2, si.v2());
   }
}
