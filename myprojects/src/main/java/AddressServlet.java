import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Anna on 2017-08-26.
 */
@WebServlet(
        name = "AddressServlet",
        urlPatterns = {"/addresses/*"}
)
public class AddressServlet extends HttpServlet {

    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/lista_ani?user=root&password=%s";
    private static final String GET_ALL_CONTACTS = "Select id_kontaktu, imie, nazwisko, lokalizacja FROM moje_kontakty";
    private static final String GET_CONTACT_BY_ID = "SELECT id_kontaktu, imie, nazwisko, lokalizacja FROM moje_kontakty WHERE id_kontaktu=%s";
    private static final String INSERT_CONTACT = "INSERT INTO moje_kontakty(imie, nazwisko, lokalizacja) VALUES('%s', '%s', '%s')";
    private static final String DELETE_CONTACT = "DELETE FROM moje_kontakty WHERE id_kontaktu=%s";

    private ObjectMapper objectMapper;

    public AddressServlet() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        checkDriver();
        try (Connection conn = createConnection();
             ServletOutputStream out = resp.getOutputStream();) {
            if (req.getPathInfo() == null) {
                returnAllContacts(conn, out);
            } else {
                String id = req.getPathInfo().replaceAll("/", "");
                returnContactById(conn, out, id);
            }
            out.flush();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        checkDriver();
        try (Connection conn = createConnection();
             ServletOutputStream out = resp.getOutputStream();) {
            Statement stmt = conn.createStatement();
            String body = getBody(req);
            Contact contact = objectMapper.readValue(body, Contact.class);
            stmt.execute(String.format(INSERT_CONTACT, contact.getFirstName(), contact.getLastName(), contact.getAddress()));
            out.write(objectMapper.writeValueAsBytes(contact));
            out.flush();
        } catch (SQLException e) {
            handleException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        checkDriver();
        try (Connection conn = createConnection();
             ServletOutputStream out = resp.getOutputStream();) {
            Statement stmt = conn.createStatement();
            String id = req.getPathInfo().replaceAll("/", "");
            stmt.execute(String.format(DELETE_CONTACT, id));
        } catch (SQLException e) {
            handleException(e);
        }
    }

    private Connection createConnection() throws SQLException, IOException {
        FileInputStream fileInputStream = new FileInputStream(new File("myprojects/src/main/resources/mypassword.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(fileInputStream)));
        String password = "";
        String line = null;
        while ((line = reader.readLine()) != null) {
            password += line;
        }
        return DriverManager.getConnection(String.format(CONNECTION_URL, password));
    }

    private String getBody(HttpServletRequest req) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    private void handleException(SQLException wyjatek) {
        System.out.println("SQLException: " + wyjatek.getMessage());
        System.out.println("SQLState: " + wyjatek.getSQLState());
        System.out.println("VendorError: " + wyjatek.getErrorCode());
    }

    private void returnContactById(Connection conn, ServletOutputStream out, String id) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(String.format(GET_CONTACT_BY_ID, id));
        while (rs.next()) {
            out.write(objectMapper.writeValueAsBytes(getNextContact(rs)));
        }
    }

    private void returnAllContacts(Connection conn, ServletOutputStream out) throws SQLException, IOException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(GET_ALL_CONTACTS);
        List<Contact> contacts = new ArrayList<>();
        while (rs.next()) {
            contacts.add(getNextContact(rs));
        }
        out.write(objectMapper.writeValueAsBytes(contacts));
    }

    static Contact getNextContact(ResultSet rs) throws SQLException {
        Contact contact = new Contact();
        contact.setId(rs.getInt(1));
        contact.setFirstName(rs.getString(2));
        contact.setLastName(rs.getString(3));
        contact.setAddress(rs.getString(4));
        return contact;
    }

    private void checkDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
