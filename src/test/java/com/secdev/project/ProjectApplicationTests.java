package com.secdev.project;

import com.secdev.project.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProjectApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void databaseConnectionTest() {
        long count = userRepository.count();
        System.out.println("User table row count: " + count);

        assertTrue(count >= 0);
    }
}
