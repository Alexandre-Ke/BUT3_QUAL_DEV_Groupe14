package com.iut.banque.util;

import com.iut.banque.cryptage.PasswordHasher;

public class HashChecker {
    public static void main(String[] args) {
        String raw = "adminpass";
        String h = PasswordHasher.hashPassword(raw);
        System.out.println("hash(adminpass) = " + h);
    }
}
