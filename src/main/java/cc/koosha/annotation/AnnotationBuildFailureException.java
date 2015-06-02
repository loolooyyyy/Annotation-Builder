package cc.koosha.annotation;

public class AnnotationBuildFailureException extends Exception {
   public AnnotationBuildFailureException() {
   }

   public AnnotationBuildFailureException(final String message) {
      super(message);
   }

   public AnnotationBuildFailureException(final String message, final Throwable cause) {
      super(message, cause);
   }

   public AnnotationBuildFailureException(final Throwable cause) {
      super(cause);
   }

   public AnnotationBuildFailureException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
