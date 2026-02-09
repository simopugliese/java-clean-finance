module com.simonepugliese.javawalletfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.javawalletfx to javafx.fxml;
    exports com.javawalletfx;
}