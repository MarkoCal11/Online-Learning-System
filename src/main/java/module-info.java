module hr.javafx.loanmanager.onlinelearningsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires com.h2database;
    requires org.slf4j;


    opens hr.javafx.onlinelearningsystem to javafx.fxml;
    exports hr.javafx.onlinelearningsystem.app;
    opens hr.javafx.onlinelearningsystem.app to javafx.fxml;
    exports hr.javafx.onlinelearningsystem.controller;
    opens hr.javafx.onlinelearningsystem.controller to javafx.fxml;
    exports hr.javafx.onlinelearningsystem.controller.student;
    opens hr.javafx.onlinelearningsystem.controller.student to javafx.fxml;
    exports hr.javafx.onlinelearningsystem.controller.teacher;
    opens hr.javafx.onlinelearningsystem.controller.teacher to javafx.fxml;
    exports hr.javafx.onlinelearningsystem.controller.admin;
    opens hr.javafx.onlinelearningsystem.controller.admin to javafx.fxml;
    exports hr.javafx.onlinelearningsystem.util;
    opens hr.javafx.onlinelearningsystem.util to javafx.fxml;
    opens hr.javafx.onlinelearningsystem.service to javafx.fxml;
    exports hr.javafx.onlinelearningsystem.service;
    exports hr.javafx.onlinelearningsystem.auth;
    exports hr.javafx.onlinelearningsystem.model;
    exports hr.javafx.onlinelearningsystem.repository;
    exports hr.javafx.onlinelearningsystem.enums;
}