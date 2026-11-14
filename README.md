# Sistema de Control de Equipos de Laboratorio

## 1. Requisitos de ejecución

Para ejecutar el proyecto se necesitan las siguientes herramientas:

- Java JDK 21
- IntelliJ IDEA
- PostgreSQL y pgAdmin 4
- Maven
- JavaFX y Hibernate ORM (dependencias incluidas en el proyecto)

## 2. Configuración local de PostgreSQL

La aplicación utiliza la base de datos `bd_laboratorio_orm` y el esquema `laboratorio`.

Para configurar la base de datos, se debe ejecutar el script SQL incluido en el proyecto, el cual contiene la creación de las tablas `tipo_equipo` y `equipo`, junto con sus registros iniciales.

La conexión se configura en `persistence.xml`, utilizando el usuario `postgres` y la variable de entorno `DB_PASSWORD` para la contraseña local.

## 3. Pasos para iniciar la aplicación

1. Abrir el proyecto en IntelliJ IDEA.
2. Cargar las dependencias de Maven.
3. Verificar que PostgreSQL esté funcionando y que la base de datos esté creada.
4. Configurar la variable de entorno `DB_PASSWORD` en IntelliJ.
5. Ejecutar la clase `LaboratorioApplication.java`.

## 4. Entidades, asociación y consultas

El proyecto utiliza dos entidades: `Equipo` y `TipoEquipo`.

La entidad `Equipo` almacena la información de cada equipo, como su código, nombre, marca, estado y cantidad. La entidad `TipoEquipo` contiene los tipos de equipos registrados.

Ambas entidades tienen una relación de uno a muchos, ya que un tipo puede tener varios equipos, pero cada equipo pertenece a un solo tipo. Esta relación se representa mediante `@ManyToOne` y `@OneToMany`.

Las consultas se realizan mediante Hibernate y JPQL. Se utilizan para listar los equipos, buscar por nombre y filtrar por tipo. También se implementan las operaciones de guardar, actualizar y eliminar registros mediante los DAO.
