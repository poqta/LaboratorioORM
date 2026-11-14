
package ni.edu.uam.laboratorioorm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ni.edu.uam.laboratorioorm.database.JPAUtil;
import ni.edu.uam.laboratorioorm.model.Equipo;
import ni.edu.uam.laboratorioorm.model.TipoEquipo;

import java.util.List;

public class EquipoDAO {

    // Guarda un equipo y lo relaciona con un tipo existente.
    public void guardar(Equipo equipo) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TipoEquipo tipo = em.find(
                    TipoEquipo.class,
                    equipo.getTipoEquipo().getIdTipo()
            );

            if (tipo == null) {
                throw new IllegalArgumentException(
                        "El tipo de equipo no existe.");
            }

            equipo.setTipoEquipo(tipo);
            em.persist(equipo);

            tx.commit();

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    // Obtiene todos los equipos con su tipo correspondiente.
    public List<Equipo> listar() {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT e FROM Equipo e " +
                            "JOIN FETCH e.tipoEquipo " +
                            "ORDER BY e.idEquipo",
                    Equipo.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // Modifica los datos de un equipo registrado.
    public void actualizar(Equipo equipo) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Equipo existente = em.find(
                    Equipo.class,
                    equipo.getIdEquipo()
            );

            if (existente == null) {
                throw new IllegalArgumentException(
                        "El equipo seleccionado no existe.");
            }

            TipoEquipo tipo = em.find(
                    TipoEquipo.class,
                    equipo.getTipoEquipo().getIdTipo()
            );

            if (tipo == null) {
                throw new IllegalArgumentException(
                        "El tipo de equipo no existe.");
            }

            existente.setCodigo(equipo.getCodigo());
            existente.setNombre(equipo.getNombre());
            existente.setMarca(equipo.getMarca());
            existente.setEstado(equipo.getEstado());
            existente.setCantidad(equipo.getCantidad());
            existente.setTipoEquipo(tipo);

            tx.commit();

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    // Elimina un equipo utilizando su ID.
    public void eliminar(Long idEquipo) {

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Equipo equipo = em.find(
                    Equipo.class, idEquipo);

            if (equipo == null) {
                throw new IllegalArgumentException(
                        "El equipo ya no existe.");
            }

            em.remove(equipo);
            tx.commit();

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    // Busca equipos por nombre y permite filtrar por tipo.
    public List<Equipo> buscar(
            String nombre, Long idTipo) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            String jpql =
                    "SELECT e FROM Equipo e " +
                            "JOIN FETCH e.tipoEquipo t " +
                            "WHERE LOWER(e.nombre) LIKE :nombre " +
                            "AND (:idTipo IS NULL OR t.idTipo = :idTipo) " +
                            "ORDER BY e.idEquipo";

            return em.createQuery(jpql, Equipo.class)
                    .setParameter(
                            "nombre",
                            "%" + (nombre == null
                                    ? ""
                                    : nombre.toLowerCase()) + "%"
                    )
                    .setParameter("idTipo", idTipo)
                    .getResultList();

        } finally {
            em.close();
        }
    }
}
