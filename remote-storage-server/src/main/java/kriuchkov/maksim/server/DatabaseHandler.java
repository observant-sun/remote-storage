package kriuchkov.maksim.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.regex.Pattern;

public class DatabaseHandler {

    private static final Logger logger = LogManager.getLogger(DatabaseHandler.class);

    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            logger.info("Connecting to database");
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/remote_storage?" +
                    "user=remote-storage&password=XgVbEF4vTzP!&serverTimezone=Europe/Moscow");
            stmt = connection.createStatement();
            logger.info("Connection to database established");
        } catch (Exception e) {
            logger.error("Connection attempt failed");
            logger.catching(e);
        }
    }

    public static void addUser(String login, String password) throws BadCredentialException {
        logger.trace("addUser() run");

        if (!Pattern.matches("^[\\d\\p{Lower}\\p{Upper}]{3,20}$", login))
            throw new BadCredentialException("login", "Логин должен состоять из латинских букв и цифр и быть от 3 до 20 символов длиной");
        if (!Pattern.matches("^[\\d\\p{Lower}\\p{Upper}]{5,32}$", password))
            throw new BadCredentialException("password", "Пароль должен состоять из латинских букв и цифр и быть от 5 до 32 символов длиной");

        try {
            String queryCheckIfOccupied = "SELECT login FROM users WHERE login = ?;";
            PreparedStatement ps = connection.prepareStatement(queryCheckIfOccupied);
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();

            if(!rs.next()) {
                String query = "INSERT INTO users (login, password) VALUES (?, ?);";
                ps = connection.prepareStatement(query);
                ps.setString(1, login);
                ps.setString(2, password);
                ps.executeUpdate();
            } else {
                throw new BadCredentialException("login", "This login is already used");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGoodCredentials(String login, String pass) {
        try {
            String query = "SELECT login FROM users WHERE login = ? AND password = ?;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                logger.debug("Successful login for " + login);
                return true;
            }
        } catch (SQLException e) {
            logger.catching(e);
        }
        logger.debug("Failed login for " + login);
        return false;
    }

    public static void disconnect() {
        logger.info("Disconnecting from database");
        try {
            connection.close();
        } catch (SQLException e) {
            logger.catching(e);
        }
    }

}
