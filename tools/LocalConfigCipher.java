import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Console;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Scanner;

/**
 * 本地测试配置加解密工具，只依赖 JDK 17。
 */
public class LocalConfigCipher {
    private static final byte[] MAGIC = "INVICTA1".getBytes(StandardCharsets.US_ASCII);
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 210_000;
    private static final Path ENV_FILE = Path.of(".env");
    private static final Path ENCRYPTED_FILE = Path.of("config", "invicta-test.env.enc");

    public static void main(String[] args) throws Exception {
        String operation = args.length == 0 ? "decrypt" : args[0];
        char[] password = readPassword(operation.equals("encrypt") ? "加密密码: " : "配置密码: ");
        try {
            if (operation.equals("decrypt")) {
                decrypt(password);
            } else if (operation.equals("encrypt")) {
                encrypt(password);
            } else {
                throw new IllegalArgumentException("仅支持 decrypt 或 encrypt");
            }
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static void encrypt(char[] password) throws Exception {
        byte[] salt = randomBytes(SALT_LENGTH);
        byte[] iv = randomBytes(IV_LENGTH);
        Cipher cipher = cipher(Cipher.ENCRYPT_MODE, password, salt, iv);
        byte[] encrypted = cipher.doFinal(Files.readAllBytes(ENV_FILE));

        ByteBuffer payload = ByteBuffer.allocate(MAGIC.length + salt.length + iv.length + encrypted.length);
        payload.put(MAGIC).put(salt).put(iv).put(encrypted);
        Files.createDirectories(ENCRYPTED_FILE.getParent());
        Files.writeString(ENCRYPTED_FILE,
                Base64.getEncoder().encodeToString(payload.array()) + System.lineSeparator(),
                StandardCharsets.US_ASCII);
        System.out.println("加密完成: " + ENCRYPTED_FILE);
    }

    private static void decrypt(char[] password) throws Exception {
        byte[] payload = Base64.getDecoder().decode(Files.readString(ENCRYPTED_FILE).trim());
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        byte[] magic = new byte[MAGIC.length];
        buffer.get(magic);
        if (!Arrays.equals(MAGIC, magic)) {
            throw new IllegalArgumentException("配置文件格式不正确");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[buffer.remaining() - SALT_LENGTH - IV_LENGTH];
        buffer.get(salt).get(iv).get(encrypted);
        byte[] plain = cipher(Cipher.DECRYPT_MODE, password, salt, iv).doFinal(encrypted);
        Files.write(ENV_FILE, plain);
        restrictPermissions();
        System.out.println("解密完成: " + ENV_FILE);
    }

    private static Cipher cipher(int mode, char[] password, byte[] salt, byte[] iv) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(keySpec).getEncoded();
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            return cipher;
        } finally {
            keySpec.clearPassword();
            Arrays.fill(key, (byte) 0);
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] value = new byte[length];
        new SecureRandom().nextBytes(value);
        return value;
    }

    private static char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            return console.readPassword(prompt);
        }
        System.out.print(prompt);
        return new Scanner(System.in).nextLine().toCharArray();
    }

    private static void restrictPermissions() {
        try {
            Files.setPosixFilePermissions(ENV_FILE, EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows 不支持 POSIX 权限，依赖当前用户目录权限保护文件。
        }
    }
}
