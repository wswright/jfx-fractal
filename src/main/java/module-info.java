module org.example {
    requires javafx.controls;
    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;
    requires io.github.classgraph;
    exports org.example;
    opens org.example.fxui to javafx.base;
    opens org.example.equations to io.github.classgraph;
    exports org.example.equations;
}