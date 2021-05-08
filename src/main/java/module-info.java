module org.example {
    requires javafx.controls;
    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;
    exports org.example;
    opens org.example.fxui to javafx.base;
}