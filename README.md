# qrcode-barcode-scanner
**The main goal of this project is to build a smart desktop application that:**

Scans QR codes and barcodes using the system’s camera or images.

Automatically detects and classifies the content (like URL, email, text, etc.) using machine learning (Weka).

Logs scan history into a local database (SQLite) for future reference.


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
            └── overlay.png          # Optional camera frame overlay image
            └── other icons/images/  
