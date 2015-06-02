# AnnotationBuilder
Easily create annotation instances in Java

example:

```java

@interface MyAnnon {
  String SomeValue() default "has default";

  int SomeInt(); // No default value.
}

// ...

@MyAnnon(SomeInt = 99)
public class DummyClass { }
// ...

final AnnotationBuilder builder = AnnotationBuilderCached.of(MyAnnon.class);
final MyAnnon ma = builder.get(null, 99);
final MyAnnon dummy = fetchDummyAnnotationFromClassByReflection(new DummyClass());
assert dummy.SomeInt() == ma.SomeInt(); // true

```

Look at tests for a complete example.

# Use case
when using Guice, instead of Names.Named("some name") you can use your own annotation.
or for anything else you can imagine (and needs instantiating annotations
of course).

```java
class MyModule extends AbstractModule {
   // Cfg is an annotation interface with  single "value" field just like
   // Guice's Named() but their namespaces wont clash.
   private final static AnnotationBuilder<Cfg> annon
         = AnnotationBuilderCached.of(Cfg.class);

   private ConstantBindingBuilder bindConstant(final String name) {
      final Cfg cfg;
      try {
         cfg = annon.get(name);
      } catch (AnnotationBuildFailureException | NoSuchMethodException e) {
         this.addError(e);
         // good idea to return null?!!
         return null;
      }
      return super.bindConstant().annotatedWith(cfg);
   }

   @Override
   protected void configure() {
      this.bindConstant("port").to(8123);
      this.bindConstant("host").to("localhost");

      this.install(NettyServerBuilderModule());
   }
}
```
