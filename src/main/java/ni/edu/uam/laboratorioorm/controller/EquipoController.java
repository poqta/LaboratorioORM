package ni.edu.uam.laboratorioorm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import ni.edu.uam.laboratorioorm.dao.EquipoDAO;
import ni.edu.uam.laboratorioorm.dao.TipoEquipoDAO;
import ni.edu.uam.laboratorioorm.model.Equipo;
import ni.edu.uam.laboratorioorm.model.TipoEquipo;

import java.util.List;
import java.util.Optional;

public class EquipoController {


    @FXML
    private TextField txtId;
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtMarca;
    @FXML
    private TextField txtCantidad;

    @FXML
    private ComboBox<TipoEquipo> cmbTipoEquipo;
    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<TipoEquipo> cmbFiltroTipo;
    @FXML
    private Button btnBuscar;

    @FXML
    private TableView<Equipo> tblEquipos;

    @FXML
    private TableColumn<Equipo, String> colCodigo;
    @FXML
    private TableColumn<Equipo, String> colNombre;
    @FXML
    private TableColumn<Equipo, String> colMarca;
    @FXML
    private TableColumn<Equipo, String> colEstado;
    @FXML
    private TableColumn<Equipo, Integer> colCantidad;
    @FXML
    private TableColumn<Equipo, String> colTipoEquipo;

    // DAO para consultar y modificar los datos.
    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();

    private final ObservableList<Equipo> listaEquipos =
            FXCollections.observableArrayList();

    private Equipo equipoSeleccionado;

    @FXML
    public void initialize() {

        txtId.setEditable(false);

        // Estados que se pueden seleccionar.
        cmbEstado.setItems(FXCollections.observableArrayList(
                "Disponible",
                "En mantenimiento",
                "Fuera de servicio"
        ));

        // Mostrar los datos de cada equipo en la tabla.
        colCodigo.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCodigo()));

        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombre()));

        colMarca.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMarca()));

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEstado()));

        colCantidad.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getCantidad()));

        colTipoEquipo.setCellValueFactory(data -> {
            TipoEquipo tipo = data.getValue().getTipoEquipo();
            return new SimpleStringProperty(
                    tipo != null ? tipo.getNombre() : "");
        });

        tblEquipos.setItems(listaEquipos);

        // Al seleccionar una fila, llenar el formulario.
        tblEquipos.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) {
                        seleccionarEquipo(actual);
                    }
                });

        // Cargar los tipos y equipos al abrir la ventana.
        try {
            cargarTipos();
            cargarEquipos();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error de conexión",
                    "No se pudieron cargar los datos: "
                            + e.getMessage());
        }
    }

    // Cargar los tipos en ambos ComboBox.
    private void cargarTipos() {

        List<TipoEquipo> tipos = tipoEquipoDAO.listar();

        cmbTipoEquipo.setItems(
                FXCollections.observableArrayList(tipos));

        ObservableList<TipoEquipo> tiposFiltro =
                FXCollections.observableArrayList();

        // Permitir buscar sin filtrar por tipo.
        tiposFiltro.add(null);
        tiposFiltro.addAll(tipos);

        cmbFiltroTipo.setItems(tiposFiltro);
        cmbFiltroTipo.setPromptText("Todos los tipos");

        cmbFiltroTipo.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(TipoEquipo tipo, boolean empty) {
                super.updateItem(tipo, empty);
                setText(empty ? null :
                        tipo == null ? "Todos los tipos" : tipo.getNombre());
            }
        });

        cmbFiltroTipo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TipoEquipo tipo, boolean empty) {
                super.updateItem(tipo, empty);
                setText(empty || tipo == null
                        ? "Todos los tipos" : tipo.getNombre());
            }
        });
    }


    // Actualizar la lista de equipos.
    private void cargarEquipos() {

        listaEquipos.setAll(equipoDAO.listar());
    }

    // Validar y guardar un nuevo equipo.
    @FXML
    private void guardarEquipo() {

        try {
            Equipo equipo = obtenerDatosFormulario();

            equipoDAO.guardar(equipo);

            limpiarFormulario();
            cargarEquipos();

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Registro exitoso",
                    "El equipo fue guardado correctamente.");

        } catch (IllegalArgumentException e) {

            mostrarAlerta(Alert.AlertType.WARNING,
                    "Datos incorrectos",
                    e.getMessage());

        } catch (Exception e) {

            if (esCodigoDuplicado(e)) {

                mostrarAlerta(Alert.AlertType.WARNING,
                        "Código duplicado",
                        "Ya existe un equipo registrado con el código "
                                + txtCodigo.getText().trim()
                                + ". Por favor, ingrese un código diferente.");

            } else {

                mostrarAlerta(Alert.AlertType.ERROR,
                        "No se pudo guardar",
                        "Ocurrió un error al registrar el equipo. "
                                + "Verifique los datos e intente nuevamente.");
            }
        }
    }


    // Actualizar el equipo seleccionado.
    @FXML
    private void actualizarEquipo() {

        if (equipoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccione un equipo",
                    "Primero seleccione un registro de la tabla.");
            return;
        }

        try {
            Equipo equipo = obtenerDatosFormulario();
            equipo.setIdEquipo(
                    equipoSeleccionado.getIdEquipo());

            equipoDAO.actualizar(equipo);

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Actualización exitosa",
                    "Los datos fueron actualizados.");

            limpiarFormulario();
            cargarEquipos();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se pudo actualizar",
                    e.getMessage());
        }
    }

    // Pedir confirmación antes de eliminar.
    @FXML
    private void eliminarEquipo() {

        if (equipoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccione un equipo",
                    "Seleccione el equipo que desea eliminar.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar este equipo?");
        confirmacion.setContentText(
                equipoSeleccionado.getNombre());

        Optional<ButtonType> respuesta =
                confirmacion.showAndWait();

        if (respuesta.isEmpty()
                || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            equipoDAO.eliminar(
                    equipoSeleccionado.getIdEquipo());

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Eliminación exitosa",
                    "El equipo fue eliminado.");

            limpiarFormulario();
            cargarEquipos();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se pudo eliminar",
                    e.getMessage());
        }
    }

    // Aplicar la búsqueda y el filtro juntos.
    @FXML
    private void buscarEquipos() {

        try {
            String nombre = txtBuscar.getText().trim();

            TipoEquipo tipo = cmbFiltroTipo.getValue();

            Long idTipo = tipo == null
                    ? null : tipo.getIdTipo();

            listaEquipos.setAll(
                    equipoDAO.buscar(nombre, idTipo));

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error en la búsqueda",
                    e.getMessage());
        }
    }

    // Limpiar los campos y la selección.
    @FXML
    private void limpiarFormulario() {

        tblEquipos.getSelectionModel().clearSelection();
        equipoSeleccionado = null;

        txtId.clear();
        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();

        cmbTipoEquipo.getSelectionModel().clearSelection();
        cmbEstado.getSelectionModel().clearSelection();
    }

    // Mostrar en el formulario el equipo seleccionado.
    private void seleccionarEquipo(Equipo equipo) {

        equipoSeleccionado = equipo;

        txtId.setText(
                String.valueOf(equipo.getIdEquipo()));
        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca());
        txtCantidad.setText(
                String.valueOf(equipo.getCantidad()));
        cmbTipoEquipo.setValue(equipo.getTipoEquipo());
        cmbEstado.setValue(equipo.getEstado());
    }

    // Validar los datos antes de guardar o actualizar.
    private Equipo obtenerDatosFormulario() {

        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();
        String cantidadTexto = txtCantidad.getText().trim();

        if (codigo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el código del equipo.");
        }
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el nombre del equipo.");
        }
        int cantidad;

        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser un número entero.");
        }
        if (cantidad < 0) {
            throw new IllegalArgumentException(
                    "La cantidad no puede ser negativa.");
        }
        TipoEquipo tipo = cmbTipoEquipo.getValue();
        if (tipo == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de equipo.");
        }
        String estado = cmbEstado.getValue();
        if (estado == null || !List.of(
                "Disponible",
                "En mantenimiento",
                "Fuera de servicio"
        ).contains(estado)) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un estado válido.");
        }
        if (codigo.length() > 30 ||
                nombre.length() > 150 ||
                marca.length() > 100) {
            throw new IllegalArgumentException(
                    "Uno de los campos supera la longitud permitida.");
        }
        return new Equipo(
                codigo,
                nombre,
                marca,
                estado,
                cantidad,
                tipo
        );
    }

    // Identificar el error de código duplicado de PostgreSQL.
    private boolean esCodigoDuplicado(Throwable error) {

        Throwable causa = error;

        while (causa != null) {

            if (causa instanceof java.sql.SQLException sqlError) {

                if ("23505".equals(sqlError.getSQLState())) {
                    String mensaje = sqlError.getMessage();

                    return mensaje != null
                            && mensaje.contains("equipo_codigo_key");
                }
            }
            causa = causa.getCause();
        }
        return false;
    }

    // Mostrar avisos y errores al usuario.
    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
