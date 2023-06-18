module bsk.project.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
        requires javafx.web;
            
        requires org.controlsfx.controls;
            requires com.dlsc.formsfx;
        
    opens bsk.project.chatapp to javafx.fxml;
    exports bsk.project.chatapp;
    exports bsk.project.chatapp.windowsControllers;
    opens bsk.project.chatapp.windowsControllers to javafx.fxml;
    exports bsk.project.chatapp.handlers;
    opens bsk.project.chatapp.handlers to javafx.fxml;
    exports bsk.project.chatapp.message;
    opens bsk.project.chatapp.message to javafx.fxml;
    exports bsk.project.chatapp.receiver;
    opens bsk.project.chatapp.receiver to javafx.fxml;
}