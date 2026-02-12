package com.secdev.project.util;

import com.secdev.project.dto.RegisterRequest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataFactory {

    public static String randomEmail() {
        return "test_" + UUID.randomUUID() + "@mail.com";
    }

    public static String randomPassword() {
        return "Pass!" + ThreadLocalRandom.current().nextInt(100000, 999999);
    }

    public static String randomPhone() {
        return "+63" + ThreadLocalRandom.current().nextInt(900000000, 999999999);
    }

    public static String randomFullName() {
        return "Test User " + ThreadLocalRandom.current().nextInt(1, 9999);
    }

    public static RegisterRequest validRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(randomEmail());
        req.setPassword(randomPassword());
        req.setPhoneNumber(randomPhone());
        req.setFullName(randomFullName());
        return req;
    }
}
