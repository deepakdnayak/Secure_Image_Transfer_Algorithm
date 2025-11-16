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

