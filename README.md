<<<<<<< HEAD

# Secure Image Encryption & Decryption using AES + RSA + HMAC  

A Java-based cryptography project that implements **hybrid encryption**, combining three algorithms — **AES**, **RSA**, and **HMAC-SHA256** — to securely encrypt and decrypt images.  

This project demonstrates confidentiality, integrity, and secure key distribution using real-world cryptographic practices inspired by TLS/SSL hybrid encryption.



## 📌 **Features**
- Encrypts image pixel data using **AES-CTR** (fast symmetric encryption)  
- Protects AES key & HMAC key using **RSA-OAEP** (asymmetric key wrapping)  
- Ensures integrity using **HMAC-SHA256**  
- Works with any `.png` image  
- Simple CLI-based encryptor & decryptor  
- Clean separation of utility code (CryptoUtils), key generation, and encrypt/decrypt logic  
- Ready to run inside **IntelliJ IDEA**

---

## 🧠 **Algorithms Used**

### 🔷 **1. AES (Advanced Encryption Standard)**
- Mode: **CTR (Counter Mode)**  
- Used for **encrypting RGB pixel bytes** of the image.  
- AES is fast and ideal for encrypting large data like images.

### 🔷 **2. RSA (Rivest-Shamir-Adleman)**
- Mode: **RSA/ECB/OAEPWithSHA-256AndMGF1Padding**  
- Used for **wrapping (encrypting)** the AES session key and HMAC key.  
- Prevents attackers from reading AES or HMAC keys even if they intercept files.

### 🔷 **3. HMAC-SHA256**
- Ensures **integrity** of encrypted pixel data.  
- Verifies that the encrypted image was **not modified** during transmission.

Together, these form a **Hybrid Cryptographic System**:
- **AES** → Encrypts image  
- **RSA** → Protects AES key and HMAC key  
- **HMAC** → Detects tampering  

This is the same pattern used in systems like HTTPS/TLS.

---

## 📁 **Project Structure**

```
/src
└── com.demo
├── CryptoUtils.java
├── KeyGen.java
├── ImageEncryptor.java
└── ImageDecryptor.java
original.png          # (you provide)
encrypted.png         # (generated after encrypting)
decrypted.png         # (output after decrypting)
public.pem            # RSA public key (generated)
private.pem           # RSA private key (generated)
wrapped_aes.key       # RSA-wrapped AES key
wrapped_hmac.key      # RSA-wrapped HMAC key
iv.bin                # AES IV file
hmac.bin              # Integrity MAC
```

---

## 🚀 **How to Run This Project (IntelliJ IDEA)**

### **1. Clone the Repository**
```sh
git clone https://github.com/<your-username>/<your-repo-name>.git
cd <your-repo-name>
```

### **2. Open in IntelliJ IDEA**
- Go to **File → Open**
- Select the project folder
- Wait for indexing to complete

### **3. Create RSA Keys**
Run the `KeyGen` class:  
**Run → Edit Configurations → + → Application**

- **Main class:** `com.demo.KeyGen`
- **Program args:** `2048` (optional)

Click **Run**.  
This creates:
```
public.pem
private.pem
```

### **🖼 4. Place Your Input Image**
Copy a `.png` file into the project root and name it:
```
original.png
```

### **🔒 5. Encrypt the Image**
Create a run config: **ImageEncryptor**

- **Main class:** `com.demo.ImageEncryptor`
- **Program arguments:**
  ```
  original.png public.pem
  ```

Run it ➜ Generates:
```
encrypted.png
wrapped_aes.key
wrapped_hmac.key
iv.bin
hmac.bin
```

### **🔓 6. Decrypt the Image**
Create another run configuration: **ImageDecryptor**

- **Main class:** `com.demo.ImageDecryptor`
- **Program arguments:**
  ```
  encrypted.png private.pem wrapped_aes.key wrapped_hmac.key iv.bin hmac.bin
  ```

Run it ➜ Generates:
```
decrypted.png
```

The decrypted image should match the original exactly.

---

## 🧪 **Testing**
After encryption & decryption:

- Try altering `encrypted.png` manually
- Run decrypt again → **HMAC verification will fail**  
  This proves integrity validation works.

---

## 🛡 **Security Notes**
This project demonstrates secure crypto patterns, but keep in mind:

- Always protect `private.pem` (never share it).
- RSA 2048-bit is secure; 4096-bit can be used for more security.
- AES-CTR does not provide integrity on its own (HMAC solves this).
- To simplify, **AES-GCM** could replace AES-CTR+HMAC (optional upgrade).

---

## 🧩 **Possible Enhancements**
- Bundle all output files into a single ZIP archive.
- Use AES-GCM authenticated encryption.
- Store RSA keys in a Java KeyStore.
- Add a Swing/JavaFX GUI for drag-and-drop encryption.
- Support multiple image formats (.jpg, .bmp).

---

## 👨‍💻 **Author**
Developed by **Deepak Nayak** and **Shashank Kamath** 
(Adapted with improvements for academic cryptography demonstration)

---

## ⭐ **Contributions**
Pull requests and suggestions are welcome.  
If this helped you, consider giving the repo a ⭐ on GitHub!

---

=======
# Image Encryption Demo

This is a simple Java-based project demonstrating image encryption and decryption using AES-256 in CTR mode. It scrambles the pixel data of a PNG image to make it unrecognizable without the correct secret key (password). The encryption is strong enough for demonstration purposes, ensuring that:
- The encrypted image appears as complete random noise (no visible features from the original).
- Decryption only succeeds with the exact same key; wrong keys result in garbage output.

This project is ideal for educational presentations on basic cryptography applied to images. Note: This is **not production-grade security**—use established libraries like Apache Commons Crypto for real applications.

## Requirements

- Java JDK 8 or higher (tested on JDK 17+).
- An IDE like IntelliJ IDEA (recommended for easy setup) or any Java compiler (e.g., `javac` for command-line).
- A PNG image file named `original.png` (any PNG works; e.g., download a sample like the Lena test image from Wikipedia).

No external dependencies—uses only standard Java libraries (`javax.crypto`, `java.awt.image`, etc.).

## Setup from GitHub Repo

1. **Clone the Repository**:
   ```
   git clone https://github.com/yourusername/image-encryption-demo.git
   ```
   Replace `yourusername` with your actual GitHub username/repo name.

2. **Open the Project in IntelliJ IDEA** (Recommended):
   - Launch IntelliJ IDEA.
   - Select "Open" and navigate to the cloned folder.
   - IntelliJ will detect it as a Java project; confirm the JDK setup if prompted.
   - If using modules, ensure the source root is set to `src`.

3. **Alternative: Command-Line Setup**:
   - Navigate to the project root: `cd image-encryption-demo`.
   - Compile the classes:
     ```
     javac src/com/demo/ImageEncryptor.java src/com/demo/ImageDecryptor.java
     ```
     (Adjust package path if different; assumes `com.demo` package.)

4. **Prepare the Source Image**:
   - Place your PNG image in the project root directory (e.g., `original.png`).
   - Recommended: Use a small-to-medium image for faster processing (e.g., 512x512 pixels).

## How to Run the Project

The project consists of two main classes:
- `ImageEncryptor`: Encrypts `original.png` and saves `encrypted.png`.
- `ImageDecryptor`: Decrypts `encrypted.png` and saves `decrypted.png`.

### Running in IntelliJ IDEA

1. **Encrypt the Image**:
   - Right-click `ImageEncryptor.java` in the project explorer.
   - Select "Run 'ImageEncryptor.main()'".
   - Enter a secret key (password) when prompted (e.g., "strongpassword123").
   - The encrypted image will be saved as `encrypted.png` in the project root.
   - Open `encrypted.png` in any image viewer (e.g., Windows Photos)—it should appear as random noise.

2. **Decrypt the Image**:
   - Copy `encrypted.png` to another location or machine if simulating transfer (optional).
   - Right-click `ImageDecryptor.java`.
   - Select "Run 'ImageDecryptor.main()'".
   - Enter the **same** secret key.
   - The decrypted image will be saved as `decrypted.png`.
   - Open `decrypted.png`—it should match `original.png` exactly.
   - Test with a wrong key to see failure (results in noise).

### Running from Command Line

1. Navigate to the project root.
2. **Encrypt**:
   ```
   java -cp src com.demo.ImageEncryptor
   ```
   Enter the key when prompted.

3. **Decrypt**:
   ```
   java -cp src com.demo.ImageDecryptor
   ```
   Enter the key when prompted.

Note: Ensure `original.png` (for encryption) or `encrypted.png` (for decryption) exists in the project root. Paths are relative; use absolute paths in code if needed.

## Project Structure

```
image-encryption-demo/
├── src/
│   └── com/
│       └── demo/
│           ├── ImageEncryptor.java
│           └── ImageDecryptor.java
├── original.png     (input image; add your own)
├── encrypted.png    (generated after encryption)
├── decrypted.png    (generated after decryption)
├── README.md
└── .git/            (Git files)
```

## Encryption Details

- **Algorithm**: AES-256 in CTR mode (stream cipher) with no padding.
- **Key Derivation**: PBKDF2 with HMAC-SHA256, using a fixed salt (for demo), high iterations (65,536), and deriving a 256-bit key + 128-bit IV from the user password.
- **Process**: Extracts RGB bytes from pixels, encrypts the byte stream, and reassembles. Alpha channel (transparency) is preserved but not encrypted.
- **Security**: Wrong passwords produce incorrect keys/IVs, leading to undecryptable noise. The encrypted image is visually indistinguishable from random data.

## Troubleshooting

- **File Not Found**: Ensure images are in the project root. Check file permissions.
- **Crypto Errors**: Verify JDK supports unrestricted policy files (for 256-bit keys). Download and install from Oracle if needed.
- **Slow Performance**: Large images take time due to pixel-by-pixel processing. Use smaller images for demos.
- **Wrong Key**: Intentional—demonstrates key sensitivity. `decrypted.png` will be scrambled.


## Contributions

Feel free to fork and submit pull requests for improvements, such as adding random salts, GUI, or support for other formats.

For questions, open an issue on GitHub.
>>>>>>> ef1c01021608eedc15cefd83d83433a885c71e3f
