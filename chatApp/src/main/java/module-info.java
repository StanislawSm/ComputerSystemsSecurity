module bsk.project.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
        requires javafx.web;
            
        requires org.controlsfx.controls;
            requires com.dlsc.formsfx;
        
    opens bsk.project.chatapp to javafx.fxml;
    exports bsk.project.chatapp;
}