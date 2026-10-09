package com.banking.security;

import org.junit.jupiter.api.Test;
import java.util.List;

public class DebugPasswordTest {

    @Test
    void testHashes() {
        String hash5 = "$2a$12$NnVCPsTa3GifwZtzJsjajubbRk66deYim5V2f7tpVA3jibAqbMQLW";
        String hash6 = "$2a$12$T5eJcuV39DSiIozar8Tzx.KfZWqxkm2glfWnuQKG4o.IF.4s1Kjru";

        List<String> candidates = List.of(
            "Password@123", "password", "password123", "Password", "Password123",
            "avadhut", "avadhut15", "avadhut@123", "avdhut15", "avdhut@123",
            "dev01", "dev01@123", "Dev01@123", "dev01123", "12345678", "123456789",
            "admin", "admin123", "Admin@123", "root@123", "root", "dev@123", "dev123"
        );

        for (String c : candidates) {
            if (PasswordUtil.checkPassword(c, hash5)) {
                System.out.println("MATCH FOR USER 5 (avdhut15): " + c);
            }
            if (PasswordUtil.checkPassword(c, hash6)) {
                System.out.println("MATCH FOR USER 6 (dev01): " + c);
            }
        }
    }
}
