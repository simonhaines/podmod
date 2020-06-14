module podmod {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
	requires javafx.base;

    opens scalardata.podmod.ui to javafx.fxml;
    opens scalardata.podmod.ui.controls to javafx.fxml;
    exports scalardata.podmod;
}