package cc.koosha.annotation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Caches result of {@code AnnotationBuilderImpl.get()} internally.
 *
 * see {@code AnnotationBuilderImpl} for more information.
 */
public final class AnnotationBuilderCached<T extends Annotation>
      implements AnnotationBuilder<T> {

   private final Object LOCK = new Object();
   private final AnnotationBuilder<T> builder;
   private final Map<List<Object>, T> cache = new WeakHashMap<>();

   private AnnotationBuilderCached(final AnnotationBuilder<T> builder) {
      this.builder = builder;
   }

   /**
    * puts {@code constructorArgs} in a {@code List} so the hashCode is
    * computed  for every value in list, then uses the key to see if an
    * instance is already available or not.
    *
    * @param ctorArgs see {@code AnnotationBuilderImpl}
    * @return see {@code AnnotationBuilderImpl}
    * @throws AnnotationBuildFailureException see {@code AnnotationBuilderImpl}
    */
   @Override
   public T get(final Object... ctorArgs) throws
         AnnotationBuildFailureException, NoSuchMethodException {

      final List<Object> key = Arrays.asList(ctorArgs);
      T cachedAnnon;

      synchronized (this.LOCK) {
         cachedAnnon = this.cache.get(key);
         if (cachedAnnon == null) {
            final T annon = this.builder.get(ctorArgs);
            this.cache.put(key, annon);
            cachedAnnon = annon;
         }
      }

      return cachedAnnon;
   }

   /**
    * Static factory method.
    *
    * Will delegate calls to AnnotationBuilderImpl.
    */
   public static <F extends Annotation> AnnotationBuilderCached<F> of(final Class<F> annonClass) {
      return new AnnotationBuilderCached<>(AnnotationBuilderImpl.of(annonClass));
   }

   @Override
   public String toString() {
      return "AnnotationBuilderCached{builder=" + this.builder + '}';
   }
}
