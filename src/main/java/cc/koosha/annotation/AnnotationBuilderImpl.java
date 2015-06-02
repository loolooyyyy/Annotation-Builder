package cc.koosha.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.Map.Entry;

/**
 * Builds an instance of an annotation interface.
 *
 * @param <T> The target annotation interface.
 */
public final class AnnotationBuilderImpl<T extends Annotation>
      implements AnnotationBuilder<T> {

   /**
    * Annotation class who we are building instances for.
    */
   private final Class<T> annonClass;

   private AnnotationBuilderImpl(final Class<T> annonClass) {
      this.annonClass = annonClass;
   }

   /**
    * Get an instance of the wrapped annotation type.
    *
    * @param ctorArgs args passed to annotation constructor. note: for using
    *                 default values defined in annotation interface, null
    *                 should be passed for that value. The length of ctorArgs
    *                 must be exactly same as number of values defined in
    *                 annotation interface, including those with default values
    *                 or else, an exception will be thrown.
    * @return Annotation instance of wrapped annon type.
    * @throws NoSuchMethodError in case of bad number of arguments or bad
    *                           argument type or null when null is not
    *                           acceptible.
    */
   @SuppressWarnings ("unchecked")
   @Override
   public T get(final Object... ctorArgs) throws NoSuchMethodException {
      final ClassLoader classLoader = this.annonClass.getClassLoader();
      final Class[] classes = {this.annonClass};
      final InvocationHandler invoked
            = new AnnonMethodInvocationHandler<>(this.annonClass, ctorArgs);

      return (T) Proxy.newProxyInstance(classLoader, classes, invoked);
   }

   /**
    * Static factory method.
    */
   public static <F extends Annotation> AnnotationBuilderImpl<F> of(final Class<F> annonClass) {
      return new AnnotationBuilderImpl<>(annonClass);
   }

   @Override
   public String toString() {
      return "AnnotationBuilderImpl{annonClass=" + this.annonClass + '}';
   }

   /**
    * Implementation of {@code InvocationHandler} for the class
    * {@code AnnotationBuilderImpl}.
    *
    * if value for a method in annon interface was provided during construction,
    * returns that value and if not, returns default value in interface.
    *
    * If a value has no default value and non was provided during construction, an
    * exception has been thrown before, already.
    *
    * @param <E> annotation type who is being implemented.
    */
   private static final class AnnonMethodInvocationHandler<E extends Annotation>
         implements InvocationHandler {
      private static final Collection<String> SPECIALS = new HashSet<>(4);
      static {
         // Methods defined in Annotation interface, are handled specially.
         for (final Method method : Annotation.class.getMethods())
            SPECIALS.add(method.getName());
      }

      /**
       * Cached result of each method in annotation interface. As the values are
       * constant, they may be cached safely.
       */
      private final Map<String, Object> invokeResult;

      /**
       * Result of hashCode(), toString() and  annotationType().
       */
      private final Map<String, Object> specialInvoke = new HashMap<>(3);

      /**
       * The annotation type who is being instantiated.
       */
      private final Class<E> annonClass;

      /**
       * Contract for hashCode is found at {@code java.lang.annotation.Annotation}
       *
       * @TODO check for array hashcode, member annon hashcode in ctor args.
       *
       * @param annonClass annon type being instantiated.
       * @param ctorArgs args to pass to annon constructor, used for annotation
       *                 fields values.
       * @throws NoSuchMethodException in case number of ctorArgs provided and
       *                               number of fields (with and without default
       *                               value) in interface do not match.
       * @throws NoSuchMethodException If null is provided for value of a field
       *                               and there is no default value for that
       *                               field in annotation interface.
       */
      AnnonMethodInvocationHandler(final Class<E> annonClass, final Object... ctorArgs)
            throws NoSuchMethodException {

         this.annonClass = annonClass;

         final Method[] methods = annonClass.getMethods();
         this.invokeResult = new HashMap<>(methods.length);

         // Excluding equals, hashcode, toString, annonType.
         final int methodLen = methods.length - 4;

         if(ctorArgs.length != methodLen)
            throw new NoSuchMethodException(
               "number of methods and number of ctor args");

         final StringBuilder toStringBuilder = new StringBuilder("@")
               .append(annonClass.getName())
               .append('(');
         int hashcode = 0;

         for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            final String methodName = method.getName();
            if(SPECIALS.contains(methodName))
               continue;

            final Object value =
                  ctorArgs[i] == null ?
                  method.getDefaultValue() :
                  ctorArgs[i];

            if(value == null)
               throw new NoSuchMethodException("null value for arg with no " +
                     "default value: " + methodName);

            this.invokeResult.put(method.getName(), value);

            toStringBuilder
                  .append(methodName)
                  .append('=')
                  .append(value)
                  .append(", ");
            hashcode += (127 * methodName.hashCode()) ^ value.hashCode();
         }

         final String substring =
               toStringBuilder.substring(0, toStringBuilder.length() - 2);
         this.specialInvoke.put("toString", substring + ')');
         this.specialInvoke.put("hashCode", hashcode);
         this.specialInvoke.put("annotationType", annonClass);
      }

      @Override
      public Object invoke(final Object proxy,
                           final Method method,
                           final Object[] args) throws NoSuchMethodException {

         final String methodName = method.getName();

         if(this.specialInvoke.containsKey(methodName))
            return this.invokeSpecial(methodName);
         else if("equals".equals(methodName))
            return this.invokeEquals(args);
         else
            return this.invokeValue(methodName, args);
      }

      private Object invokeSpecial(final String methodName) {
         return this.specialInvoke.get(methodName);
      }

      private Object invokeValue(final String methodName, final Object... args)
            throws NoSuchMethodException {
         if(args != null)
            if(args.length != 0)
               throw new NoSuchMethodException("no such method, matching this number of args");

         return this.invokeResult.get(methodName);
      }

      /**
       * @TODO equals for multiple arg exception.
       * @TODO equals instance of.
       */
      private boolean invokeEquals(final Object... args) throws NoSuchMethodException {
         if(args == null || args.length != 1 || args[0] == null)
            throw new NoSuchMethodException("equals method takes exactly one arg");
         final Object o = args[0];

         if(!this.annonClass.isAssignableFrom(o.getClass()))
            return false;

         try {
            for (final Entry<String, Object> e : this.invokeResult.entrySet()) {
               final Object otherInvoke
                     = o.getClass().getMethod(e.getKey()).invoke(null);
               if(!Objects.equals(otherInvoke, e.getValue()))
                  return false;
            }
         }
         catch (final Exception ignored) {
            return false;
         }

         return true;
      }
   }
}
