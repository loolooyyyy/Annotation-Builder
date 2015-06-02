package cc.koosha.annotation;

import java.lang.annotation.Annotation;


@FunctionalInterface
public interface AnnotationBuilder<T extends Annotation> {
   /**
    * @TODO better exception?
    */
   T get(Object... ctorArgs)
         throws AnnotationBuildFailureException, NoSuchMethodException;
}
