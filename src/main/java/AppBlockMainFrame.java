import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class AppBlockMainFrame extends JFrame implements ActionListener {
    JButton btnSelectFile, btnSaveFile, btnAppBlock, btnAppUnBlock;
    JTextField txtSelectedFile;
    JTextArea txtSavedFiles;
    JFileChooser fileChooser;
    String selectedFilePath="";
    Connection conn;
    Statement stmt;
    String[] appBlocks;
    public AppBlockMainFrame() {
        super("File Chooser");

        // Create UI components
        btnSelectFile = new JButton("Select File");
        btnSaveFile = new JButton("Save File");
        btnAppBlock = new JButton("Block App");
        btnAppUnBlock = new JButton("Unblock App");
        txtSelectedFile = new JTextField(30);
        txtSavedFiles = new JTextArea(10,30);
        txtSavedFiles.setLineWrap(true);
        txtSavedFiles.setWrapStyleWord(true);

        // Set action listeners
        btnSelectFile.addActionListener(this);
        btnSaveFile.addActionListener(this);
        btnAppBlock.addActionListener(this);
        btnAppUnBlock.addActionListener(this);
        // Set layout
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(btnSelectFile);
        topPanel.add(txtSelectedFile);
        add(topPanel, BorderLayout.NORTH);
//        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        bottomPanel.add(btnSaveFile);
//        bottomPanel.add(btnAppBlock);
//        bottomPanel.add(btnAppUnBlock);
//        bottomPanel.add(new JScrollPane(txtSavedFiles));
//        add(bottomPanel, BorderLayout.SOUTH);
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 0, 0, 10); // Add some padding between buttons
        bottomPanel.add(btnSaveFile, c);

        c.gridx = 0;
        c.gridy = 1;
        bottomPanel.add(btnAppBlock, c);

        c.gridx = 0;
        c.gridy = 2;
        bottomPanel.add(btnAppUnBlock, c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 3; // Make the text area span across three rows
        c.fill = GridBagConstraints.BOTH;
        bottomPanel.add(new JScrollPane(txtSavedFiles), c);

        add(bottomPanel, BorderLayout.SOUTH);

        // Set up database connection
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:appblock.db");
            stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS filedir (id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database!");
            System.exit(0);
        }

        // Load saved files
        try {
            ResultSet rs = stmt.executeQuery("SELECT path FROM filedir");
            while (rs.next()) {
                String path = rs.getString("path");
                txtSavedFiles.setText(txtSavedFiles.getText() + path + "\n");
            }
            appBlocks = txtSavedFiles.getText().split("\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set window properties
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSelectFile) {
            fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedFilePath = selectedFile.getAbsolutePath();
                txtSelectedFile.setText(selectedFilePath);
            }
        } else if (e.getSource() == btnSaveFile) {
            try {
                if(!selectedFilePath.isEmpty() && new File(selectedFilePath).exists()) {
                    stmt.executeUpdate("INSERT INTO filedir (path) VALUES ('" + selectedFilePath + "')");
                    txtSavedFiles.setText(txtSavedFiles.getText() + selectedFilePath + "\n");
                    txtSelectedFile.setText("");
                    appBlocks = txtSavedFiles.getText().split("\n");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving file path to database!");
            }
        } else if (e.getSource() == btnAppBlock) {
            try {
                for(int i = 0; i < appBlocks.length; i ++) {
                    List<String> cmd = Arrays.asList("icacls", appBlocks[i], "/deny", "Everyone:(RX)");
                    ProcessBuilder builder = new ProcessBuilder(cmd);
                    Process process = builder.start();
                    process.waitFor();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } else if (e.getSource() == btnAppUnBlock) {
            try {
                for(int i = 0; i < appBlocks.length; i ++) {
                    List<String> cmd = Arrays.asList("icacls", appBlocks[i], "/remove:d", "Everyone");
                    ProcessBuilder builder = new ProcessBuilder(cmd);
                    Process process = builder.start();
                    process.waitFor();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppBlockMainFrame();
    }
}

