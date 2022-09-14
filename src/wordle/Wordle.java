package wordle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Wordle extends Application {

    //p palabras, l letras, t teclas
    private static int p = 6, l = 5, t = 27;

    //Contador de palabras y de letras
    private static int lJuego = 0, pJuego = 0;

    //Palabras y teclas jugadas
    private static Label letras[][];
    private static Label teclas[];

    private static String palabra = "";

    private static String usuario = "wordle";
    private static String passwd = "wordle";
    private static String cadConex = "jdbc:mysql://localhost:3306/";
    private static String baseDeDatos = "wordle";

    private static int[] mapa = {0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0,
        0, 0, 0};

    private static GridPane tablero, teclado;
    private static VBox principal;

    public static void main(String[] args) {
        inicializar();
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        /*Extrae la palabras del fichero.txt donde estan todas las palabras de 5 letras
        y las añade a la base de datos.*/
        //importarInformacion();

        tablero = new GridPane();
        tablero.setVgap(4);
        tablero.setHgap(4);
        tablero.setAlignment(Pos.CENTER);

        teclado = new GridPane();
        teclado.setHgap(4);
        teclado.setVgap(4);
        teclado.setAlignment(Pos.CENTER);

        principal = new VBox(tablero, teclado);
        principal.setAlignment(Pos.CENTER);
        principal.setPadding(new Insets(10));
        principal.setSpacing(20);

        //Le damos al VBox un estilo de CSS.
        principal.getStylesheets().clear();
        principal.getStylesheets().add("/estilos/estilo.css");

        //Rellena el teclado con las letras creadas de forma personalizada.
        rellenarTeclado(teclado);

        //Rellena el tablero con " - ".
        rellenarTablero(tablero);

        palabraAJugar();

        stage.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            if (event.getCode() == KeyCode.ENTER && lJuego == 5) {
                if (existePalabra()) {
                    resolverPalabra();
                    revisarGanar();
                    if (pJuego == 6) {
                        juegoPerdido();
                    }
                }else{
                    noExistePalabra();
                }

            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                borrarLetra();
            } else if (event.getCode().isLetterKey()) {
                ponLetra(event.getCode().toString());
            } else if (event.getText().equals("ñ")) {
                ponLetra("Ñ");
            } else {
                System.out.println("Sin efecto.");
            }
        });

        Scene escena = new Scene(principal, 425, 575);
        stage.setScene(escena);
        stage.setTitle("Wordle");
        stage.show();
    }

    private static void inicializar() {
        letras = new Label[p][l];
        teclas = new Label[t];
    }

    private void importarInformacion() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = (Connection) DriverManager.getConnection(cadConex + baseDeDatos, usuario, passwd);
            System.out.println("Conectando...");

            Statement st = con.createStatement();

            BufferedReader fichero = new BufferedReader(new FileReader("C:\\Users\\zx21student278\\Desktop\\fichero.txt"));

            String linea = fichero.readLine();
            ArrayList<String> lista = new ArrayList<>();
            while (linea != null) {
                lista.add(linea);
                linea = fichero.readLine();
            }

            //Vaciamos la tabla primero en caso de que haya algo dentro
            String borrado = "TRUNCATE TABLE wordle";
            int borrar = st.executeUpdate(borrado);

            //Escribimos todas las palabras dentro de la tabla
            for (int i = 0; i < lista.size(); i++) {
                String accesoBBDD = "INSERT INTO wordle VALUES ('" + lista.get(i) + "')";
                int result = st.executeUpdate(accesoBBDD);
            }

            con.close();
            System.out.println("Desconectando...");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void rellenarTeclado(GridPane teclado) {

        teclas[0] = new Label("Q");
        teclas[1] = new Label("W");
        teclas[2] = new Label("E");
        teclas[3] = new Label("R");
        teclas[4] = new Label("T");
        teclas[5] = new Label("Y");
        teclas[6] = new Label("U");
        teclas[7] = new Label("I");
        teclas[8] = new Label("O");
        teclas[9] = new Label("P");

        teclas[10] = new Label("A");
        teclas[11] = new Label("S");
        teclas[12] = new Label("D");
        teclas[13] = new Label("F");
        teclas[14] = new Label("G");
        teclas[15] = new Label("H");
        teclas[16] = new Label("J");
        teclas[17] = new Label("K");
        teclas[18] = new Label("L");
        teclas[19] = new Label("Ñ");

        teclas[20] = new Label("Z");
        teclas[21] = new Label("X");
        teclas[22] = new Label("C");
        teclas[23] = new Label("V");
        teclas[24] = new Label("B");
        teclas[25] = new Label("N");
        teclas[26] = new Label("M");

        for (int i = 0; i < 10; i++) {
            teclado.add(teclas[i], i, 0);
            teclas[i].setMinSize(35, 35);
            teclas[i].setAlignment(Pos.CENTER);
        }

        int cont = 10;
        for (int i = 0; i < 10; i++) {
            teclado.add(teclas[cont], i, 1);
            teclas[cont].setMinSize(35, 35);
            teclas[cont].setAlignment(Pos.CENTER);
            cont++;
        }

        for (int i = 0; i < 7; i++) {
            teclado.add(teclas[cont], i, 2);
            teclas[cont].setMinSize(35, 35);
            teclas[cont].setAlignment(Pos.CENTER);
            cont++;
        }
    }

    private void rellenarTablero(GridPane tablero) {
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < l; j++) {
                letras[i][j] = new Label("");
                letras[i][j].setMinSize(45, 45);
                letras[i][j].setAlignment(Pos.CENTER);
                tablero.add(letras[i][j], j, i);
            }
        }
    }

    private void resolverPalabra() {

        char temp;
        for (lJuego = 0; lJuego <= 4; lJuego++) {
            for (int i = 0; i <= 4; i++) {
                //Transformar de string a char para comparar.
                temp = letras[pJuego][lJuego].getText().charAt(0);
                if (temp == palabra.charAt(i)) {
                    if (lJuego == i) {

                        for (int j = 0; j <= 26; j++) {
                            if (teclas[j].getText().charAt(0) == temp) {
                                mapa[j] = 3;
                            }
                        }

                        letras[pJuego][lJuego].getStyleClass().remove("Amarillo");
                        letras[pJuego][lJuego].getStyleClass().remove("Gris");
                        letras[pJuego][lJuego].getStyleClass().add("Verde");
                        break;
                    } else {

                        for (int j = 0; j <= 26; j++) {
                            if (teclas[j].getText().charAt(0) == temp) {
                                if (mapa[j] != 3) {
                                    mapa[j] = 2;
                                }
                            }
                        }

                        letras[pJuego][lJuego].getStyleClass().remove("Verde");
                        letras[pJuego][lJuego].getStyleClass().remove("Gris");
                        letras[pJuego][lJuego].getStyleClass().add("Amarillo");
                        break;
                    }
                }
            }
            for (int j = 0; j <= 26; j++) {
                if (teclas[j].getText().charAt(0) == letras[pJuego][lJuego].getText().charAt(0)) {
                    if (mapa[j] != 3) {
                        if (mapa[j] != 2) {
                            letras[pJuego][lJuego].getStyleClass().remove("Verde");
                            letras[pJuego][lJuego].getStyleClass().remove("Amarillo");
                            letras[pJuego][lJuego].getStyleClass().add("Gris");
                            mapa[j] = 1;
                        }
                    }
                }
            }
        }

        for (int i = 0; i <= 26; i++) {
            if (mapa[i] == 1) {
                teclas[i].getStyleClass().add("Gris");
            }
            if (mapa[i] == 2) {
                teclas[i].getStyleClass().remove("Gris");
                teclas[i].getStyleClass().add("Amarillo");
            }
            if (mapa[i] == 3) {
                teclas[i].getStyleClass().remove("Gris");
                teclas[i].getStyleClass().remove("Amarillo");
                teclas[i].getStyleClass().add("Verde");
            }
        }

        lJuego = 0;
        pJuego++;
    }

    private void borrarLetra() {
        if (lJuego > 0) {
            lJuego--;
            letras[pJuego][lJuego].setText("");
            letras[pJuego][lJuego].getStyleClass().clear();
        }
    }

    private void ponLetra(String letra) {
        if (lJuego <= 4) {
            letras[pJuego][lJuego].setText(letra);
            letras[pJuego][lJuego].getStyleClass().clear();
            letras[pJuego][lJuego].getStyleClass().add("BordeOscuro");
            lJuego++;
        }
    }

    private void revisarGanar() {

        String palabraIngresada = letras[pJuego - 1][0].getText() + letras[pJuego - 1][1].getText()
                + letras[pJuego - 1][2].getText() + letras[pJuego - 1][3].getText()
                + letras[pJuego - 1][4].getText();

        if (palabraIngresada.equals(palabra)) {

            Alert alerta = new Alert(AlertType.NONE);
            alerta.setHeaderText("Adivinaste la palabra!");
            alerta.setTitle("ENHORABUENA");
            ButtonType btnSi = new ButtonType("Jugar más", ButtonData.YES);
            ButtonType btnNo = new ButtonType("Salir", ButtonData.NO);

            alerta.getButtonTypes().setAll(btnSi, btnNo);
            Optional<ButtonType> action = alerta.showAndWait();

            if (action.get() == btnNo) {
                System.exit(0);
            }
            if (action.get() == btnSi) {
                for (int i = 0; i < mapa.length; i++) {
                    mapa[i] = 0;
                }
                pJuego = 0;
                lJuego = 0;
                tablero.getChildren().clear();
                teclado.getChildren().clear();
                rellenarTeclado(teclado);
                rellenarTablero(tablero);
                palabraAJugar();
                principal.getStylesheets().clear();
                principal.getStylesheets().add("/estilos/estilo.css");
            }
        }
    }

    private void juegoPerdido() {
        Alert alerta = new Alert(AlertType.NONE);
        alerta.setHeaderText("No has podido adivinar la palabra...");
        alerta.setTitle("Game over");
        ButtonType btnSi = new ButtonType("Jugar más", ButtonData.YES);
        ButtonType btnNo = new ButtonType("Salir", ButtonData.NO);

        alerta.getButtonTypes().setAll(btnSi, btnNo);
        Optional<ButtonType> action = alerta.showAndWait();

        if (action.get() == btnNo) {
            System.exit(0);
        }
        if (action.get() == btnSi) {
            for (int i = 0; i < mapa.length; i++) {
                mapa[i] = 0;
            }
            pJuego = 0;
            lJuego = 0;
            tablero.getChildren().clear();
            teclado.getChildren().clear();
            rellenarTeclado(teclado);
            rellenarTablero(tablero);
            palabraAJugar();
            principal.getStylesheets().clear();
            principal.getStylesheets().add("/estilos/estilo.css");
        }
    }

    private static void palabraAJugar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = (Connection) DriverManager.getConnection(cadConex + baseDeDatos, usuario, passwd);

            int posicion = (new Random()).nextInt(10835);

            PreparedStatement contarPalabras = con.prepareStatement("SELECT * FROM wordle LIMIT " + posicion + ", 1");

            ResultSet rs = contarPalabras.executeQuery();

            rs.next();
            palabra = rs.getString("palabras");
            System.out.println(palabra);
            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private boolean existePalabra() {

        String palabraIngresada = letras[pJuego][0].getText() + letras[pJuego][1].getText()
                + letras[pJuego][2].getText() + letras[pJuego][3].getText()
                + letras[pJuego][4].getText();

        
        boolean salida = true;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = (Connection) DriverManager.getConnection(cadConex + baseDeDatos, usuario, passwd);

            int posicion = (new Random()).nextInt(10835);

            PreparedStatement contarPalabras = con.prepareStatement("SELECT count(*) as existe FROM wordle WHERE palabras like '" + palabraIngresada + "'");

            ResultSet rs = contarPalabras.executeQuery();

            rs.next();
            String existe = rs.getString("existe");

            if (existe.equals("0")) {
                salida = false;
            }
            con.close();
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return salida;
    }

    private void noExistePalabra() {
        Alert alerta = new Alert(AlertType.NONE);
            alerta.setHeaderText("Esta palabra no existe.");
            alerta.setTitle("Warning.");
            ButtonType btnSi = new ButtonType("Entendido", ButtonData.YES);

            alerta.getButtonTypes().setAll(btnSi);
            Optional<ButtonType> action = alerta.showAndWait();
    
    }
}
