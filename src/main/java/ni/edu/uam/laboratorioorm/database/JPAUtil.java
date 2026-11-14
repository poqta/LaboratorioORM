
package ni.edu.uam.laboratorioorm.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    private static EntityManagerFactory entityManagerFactory;

    // Inicializa Hibernate y configura la conexión a PostgreSQL.
    public static synchronized void iniciar() {

        if (entityManagerFactory == null) {

            String password = System.getenv("DB_PASSWORD");

            if (password == null || password.isEmpty()) {
                throw new RuntimeException("DB_PASSWORD inválido.");
            }

            Map<String, Object> propiedades = new HashMap<>();

            propiedades.put(
                    "jakarta.persistence.jdbc.password",
                    password
            );

            entityManagerFactory =
                    Persistence.createEntityManagerFactory(
                            "LaboratorioPU", propiedades
                    );
        }
    }

    // Obtiene un EntityManager para trabajar con la base de datos.
    public static EntityManager getEntityManager() {

        iniciar();
        return entityManagerFactory.createEntityManager();
    }

    // Cierra los recursos de Hibernate al finalizar la aplicación.
    public static synchronized void cerrar() {

        if (entityManagerFactory != null
                && entityManagerFactory.isOpen()) {

            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}
