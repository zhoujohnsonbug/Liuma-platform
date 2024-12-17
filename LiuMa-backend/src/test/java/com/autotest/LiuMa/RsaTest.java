package com.autotest.LiuMa;


import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaTest {
    public static void main(String[] args) throws Exception {
        // 已知公钥字符串
        String publicKeyString = "-----BEGIN PUBLIC KEY-----\n" +
                "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMBOaPaQqhDugAWs633nE3hadm8K6HDYias7sOxd1rj1iXxl0fikWnXfVYsDnBn/DVrvxnCXwD9/JhieBvcqQcsCAwEAAQ==\n" +
                "-----END PUBLIC KEY-----";

        // 将公钥字符串解码为字节数组
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----\n", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", ""));

        // 使用 X509EncodedKeySpec 将公钥字节数组转换为 PublicKey 对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // 准备待加密的数据和密码
        String data = "12345678";
        byte[] passwordBytes = data.getBytes(StandardCharsets.UTF_8);

        // 使用 RSA 加密算法和 PKCS1v15 填充模式进行加密
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(passwordBytes);

        // 将加密后的数据进行 Base64 编码
        String encryptedData = Base64.getEncoder().encodeToString(encryptedBytes);

        System.out.println("加密前的数据：" + data);
        System.out.println("加密后的数据：" + encryptedData);
    }
}
