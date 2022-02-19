module org.example {
    requires javafx.controls;
    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;
    requires io.github.classgraph;
    requires org.example.fractal.lib;
    requires org.example.fractal.equations;
    exports org.wswright;
    opens org.wswright.fxui to javafx.base;
    uses org.wswright.fractal.lib.IFractalEquation;
}