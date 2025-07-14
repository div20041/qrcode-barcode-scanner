package com.example;

import org.bytedeco.javacv.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.common.BitMatrix;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.converters.ConverterUtils.DataSource;

import javax.imageio.ImageIO;

public class App {

    private static final String DB_URL = "jdbc:sqlite:scan_results.db";

    public static void main(String[] args) throws Exception {
         try {
        // Set FlatLaf Dark Look and Feel
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
    } catch (Exception e) {
        System.err.println("Failed to initialize dark theme: " + e.getMessage());
    }
        setupDatabase();

        while (true) {
            String[] options = {
                    "Scan from Camera",
                    "Scan from Image File",
                    "Scan from Clipboard",
                    "View Scan History",
                    "Generate QR Code",
                    "Export to CSV",
                    "Exit"
            };

            int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "QR & Barcode Scanner",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            switch (choice) {
                case 0 -> scanFromCamera();
                case 1 -> scanFromFile();
                case 2 -> scanFromClipboard();
                case 3 -> showScanHistory();
                case 4 -> generateQRCode();
                case 5 -> exportToCSV();
                default -> System.exit(0);
            }
        }
    }

    public static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "CREATE TABLE IF NOT EXISTS scans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "timestamp TEXT," +
                    "content TEXT)";
            conn.createStatement().execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "DB Error: " + e.getMessage());
        }
    }

    public static void logResult(String content) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO scans (timestamp, content) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setString(2, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error logging result: " + e.getMessage());
        }
    }

  


    public static void scanFromFile() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "bmp", "gif"));
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                BufferedImage img = ImageIO.read(file);
                Result qrResult = decodeCode(img);

                if (qrResult != null) {
                    String text = qrResult.getText();
                    String category = classifyContent(text);
                    if (category.equals("URL") && checkIfSuspiciousURL(text)) {
    JOptionPane.showMessageDialog(null, "⚠️ Warning: This link may be suspicious!\n" + text, "Phishing Alert", JOptionPane.WARNING_MESSAGE);
}

                   // speakText("Detected " + category + ". Content: " + text);
String message = "Detected: " + text + "\nType: " + category;
JOptionPane.showMessageDialog(null, message);
if (category.equals("URL")) {
    int open = JOptionPane.showConfirmDialog(null, "Open this URL?\n" + text, "Open Link", JOptionPane.YES_NO_OPTION);
    if (open == JOptionPane.YES_OPTION) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(text));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open URL: " + e.getMessage());
        }
    }
} else if (category.equals("Email")) {
    int open = JOptionPane.showConfirmDialog(null, "Open mail client to send to:\n" + text, "Send Email", JOptionPane.YES_NO_OPTION);
    if (open == JOptionPane.YES_OPTION) {
        try {
            Desktop.getDesktop().mail(new java.net.URI("mailto:" + text));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open email client: " + e.getMessage());
        }
    }
}


                    logResult(text);
                    copyToClipboard(text);
                    playBeep();
                    JOptionPane.showMessageDialog(null, "Detected: " + text);
                } else {
                    JOptionPane.showMessageDialog(null, "No code found.");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public static void scanFromClipboard() {
        try {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
                BufferedImage img = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();

                Result result = decodeCode(img);
                if (result != null) {
                    String text = result.getText();
                    String category = classifyContent(text);
                    if (category.equals("URL") && checkIfSuspiciousURL(text)) {
    JOptionPane.showMessageDialog(null, "⚠️ Warning: This link may be suspicious!\n" + text, "Phishing Alert", JOptionPane.WARNING_MESSAGE);
}

                   // speakText("Detected " + category + ". Content: " + text);
String message = "Detected: " + text + "\nType: " + category;
JOptionPane.showMessageDialog(null, message);
if (category.equals("URL")) {
    int open = JOptionPane.showConfirmDialog(null, "Open this URL?\n" + text, "Open Link", JOptionPane.YES_NO_OPTION);
    if (open == JOptionPane.YES_OPTION) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(text));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open URL: " + e.getMessage());
        }
    }
} else if (category.equals("Email")) {
    int open = JOptionPane.showConfirmDialog(null, "Open mail client to send to:\n" + text, "Send Email", JOptionPane.YES_NO_OPTION);
    if (open == JOptionPane.YES_OPTION) {
        try {
            Desktop.getDesktop().mail(new java.net.URI("mailto:" + text));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open email client: " + e.getMessage());
        }
    }
}


                    logResult(text);
                    copyToClipboard(text);
                    playBeep();
                    JOptionPane.showMessageDialog(null, "Clipboard Detected: " + text);
                } else {
                    JOptionPane.showMessageDialog(null, "No code in clipboard image.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Clipboard doesn't contain an image.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Clipboard scan error: " + e.getMessage());
        }
    }

    public static Result decodeCode(BufferedImage image) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        return new MultiFormatReader().decode(bitmap);
    }

    public static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }

    public static void playBeep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public static void showScanHistory() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT timestamp, content FROM scans ORDER BY id DESC");

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Timestamp");
            columnNames.add("Content");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("timestamp"));
                row.add(rs.getString("content"));
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            JTextField searchField = new JTextField();
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

                public void filter() {
                    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                    table.setRowSorter(sorter);
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
                }
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(searchField, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(null, panel, "Scan History (Searchable)", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "History Error: " + e.getMessage());
        }
    }

    public static void exportToCSV() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PrintWriter writer = new PrintWriter(new FileWriter("scan_history.csv"))) {

            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM scans");
            writer.println("Timestamp,Content");
            while (rs.next()) {
                writer.printf("%s,%s%n", rs.getString("timestamp"), rs.getString("content"));
            }
            JOptionPane.showMessageDialog(null, "Exported to scan_history.csv");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "CSV Export error: " + e.getMessage());
        }
    }

    public static void generateQRCode() {
        String text = JOptionPane.showInputDialog("Enter text to generate QR:");
        if (text == null || text.isEmpty()) return;

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("qr_output.png"));
            int option = fileChooser.showSaveDialog(null);

            if (option == JFileChooser.APPROVE_OPTION) {
                ImageIO.write(img, "png", fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(null, "QR Code saved.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "QR Generation error: " + e.getMessage());
        }
    }
 public static String classifyContent(String text) {
    try {
        // Load ARFF file from classpath
        InputStream inputStream = App.class.getClassLoader().getResourceAsStream("qr_content.arff");
        if (inputStream == null) {
            throw new IOException("ARFF file not found in classpath!");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Instances data = new Instances(reader);
        data.setClassIndex(data.numAttributes() - 1);

        //Classifier cls = new J48(); // Or use NaiveBayes
        Classifier cls = new weka.classifiers.bayes.NaiveBayesMultinomialText();

        cls.buildClassifier(data);

        // Create instance
        Instance inst = new DenseInstance(2);
        inst.setDataset(data);
        inst.setValue(data.attribute(0), text);

        double prediction = cls.classifyInstance(inst);
        return data.classAttribute().value((int) prediction);

    } catch (Exception e) {
        e.printStackTrace();
        return "unknown";
    }
}

public static void scanFromCamera() throws Exception {
    OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    grabber.start();

    CanvasFrame canvas = new CanvasFrame("Camera Scanner");
    canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Java2DFrameConverter converter = new Java2DFrameConverter();

    // Create overlay frame for green light
    JFrame overlay = new JFrame();
    overlay.setUndecorated(true);
    overlay.setAlwaysOnTop(true);
    overlay.setSize(150, 50);
    overlay.setLocation(canvas.getX() + 10, canvas.getY() + 30);
    overlay.getContentPane().setBackground(Color.BLACK);

    JLabel greenLight = new JLabel("● Scanning...");
    greenLight.setForeground(Color.GREEN);
    greenLight.setFont(new Font("Arial", Font.BOLD, 20));
    overlay.add(greenLight);
    overlay.setVisible(true);

    String lastScannedText = "";
    long lastScanTime = 0;

    while (canvas.isVisible()) {
    org.bytedeco.javacv.Frame frame = grabber.grab();
    BufferedImage img = converter.getBufferedImage(frame);

    if (img != null) {
        // Draw a scanner rectangle on top
        Graphics2D g = img.createGraphics();
        g.setColor(Color.GREEN);  // Use neon green or red
        g.setStroke(new BasicStroke(4));
        
        int boxWidth = 300;
        int boxHeight = 300;
        int x = (img.getWidth() - boxWidth) / 2;
        int y = (img.getHeight() - boxHeight) / 2;
        g.drawRect(x, y, boxWidth, boxHeight);

        // Optional: Add "Scanning..." text
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Scanning...", x + 80, y - 10);

        g.dispose();

        try {
            Result result = decodeCode(img);
            if (result != null) {
                String text = result.getText();
                long now = System.currentTimeMillis();

                if (!text.equals(lastScannedText) || (now - lastScanTime > 3000)) {
                    lastScannedText = text;
                    lastScanTime = now;

                    String category = classifyContent(text);
                    if (category.equals("URL") && checkIfSuspiciousURL(text)) {
    JOptionPane.showMessageDialog(null, "⚠️ Warning: This link may be suspicious!\n" + text, "Phishing Alert", JOptionPane.WARNING_MESSAGE);
}

                    //speakText("Detected " + category + ". Content: " + text);
                    logResult(text);
                    copyToClipboard(text);
                    playBeep();

                    String message = "Detected: " + text + "\nType: " + category;
                    JOptionPane.showMessageDialog(null, message);

                    if (category.equals("URL")) {
                        int open = JOptionPane.showConfirmDialog(null, "Open this URL?\n" + text, "Open Link", JOptionPane.YES_NO_OPTION);
                        if (open == JOptionPane.YES_OPTION) {
                            Desktop.getDesktop().browse(new java.net.URI(text));
                        }
                    } else if (category.equals("Email")) {
                        int open = JOptionPane.showConfirmDialog(null, "Send email to:\n" + text, "Open Mail", JOptionPane.YES_NO_OPTION);
                        if (open == JOptionPane.YES_OPTION) {
                            Desktop.getDesktop().mail(new java.net.URI("mailto:" + text));
                        }
                    }
                }
            }
        } catch (NotFoundException ignored) {
            // No QR found
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    canvas.showImage(converter.convert(img));
}

}
public static boolean checkIfSuspiciousURL(String url) {
    String[] suspiciousPatterns = {
        "bit.ly", "tinyurl.com", "ow.ly", "goo.gl", "t.co",
        "freegift", "verify", "login", "secure", "update", "confirm"
    };

    url = url.toLowerCase();
    for (String pattern : suspiciousPatterns) {
        if (url.contains(pattern)) {
            return true;
        }
    }

    try {
        java.net.URI uri = new java.net.URI(url);
        String host = uri.getHost();
        if (host != null && (host.endsWith(".xyz") || host.endsWith(".tk"))) {
            return true; // suspicious free TLDs
        }
    } catch (Exception ignored) {}

    return false;
}

}


