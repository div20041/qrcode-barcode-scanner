# qrcode-barcode-scanner
**The main goal of this project is to build a smart desktop application that:**

Scans QR codes and barcodes using the system’s camera or images.

Automatically detects and classifies the content (like URL, email, text, etc.) using machine learning (Weka).

Logs scan history into a local database (SQLite) for future reference.

**all required installations**
✅ 1. Java JDK (Java Development Kit)
Required to write and run Java code.

📥 Download Java JDK
(Choose JDK 17 or JDK 21 for long-term support)

✅ 2. Apache Maven
Helps manage project libraries (dependencies) and build your app.

📥 Download Maven

📘 Installation guide: Install Maven

✅ 3. IDE (Integrated Development Environment)
Makes it easier to write and run Java code.

Recommended IDEs:

📥 IntelliJ IDEA Community (Free)

📥 Eclipse IDE for Java

✅ 4. ZXing (Zebra Crossing) – QR Code & Barcode Scanner
No need to manually install — Maven handles it using this dependency:

✅ 5. JavaCV (Webcam Capture Support)
Required for scanning QR codes from your webcam.

✅ 6. WEKA (Machine Learning Classifier)
Helps in classifying content (e.g., whether it’s an email, link, phone number, etc.).

📥 Download WEKA GUI (optional)

✅ 7. SQLite (for storing scan history)
No installation needed — uses SQLite JDBC driver.

qrscanner/
├── pom.xml                           # Maven configuration file
├── scan_history.csv                  # (Generated at runtime if exporting)
├── README.md                         # (Optional: project explanation file)

└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── App.java      # Main application class
        │           
        │
        └── resources/
            └── qr_content.arff      # Weka dataset file

 ✅ Prerequisites
Make sure these are already installed:

Java JDK (java -version)

Maven (mvn -version)

🛠️ 1. Compile the Project
bash
Copy
Edit
mvn clean compile
This will:

Download all dependencies

Compile your Java files in src/main/java

🚀 2. Run the Application
bash
Copy
Edit
mvn exec:java -Dexec.mainClass="com.example.App"
Replace com.example.App with your main class if it's different.
            └── overlay.png          # Optional camera frame overlay image
            └── other icons/images/  
