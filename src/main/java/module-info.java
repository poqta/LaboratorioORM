
module ni.edu.uam.laboratorioorm {

    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens ni.edu.uam.laboratorioorm.model
            to org.hibernate.orm.core, javafx.base;

    opens ni.edu.uam.laboratorioorm.controller
            to javafx.fxml;

    exports ni.edu.uam.laboratorioorm;
}
