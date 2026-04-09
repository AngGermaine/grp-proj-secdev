package com.secdev.project.config;

import com.secdev.project.model.Role;
import com.secdev.project.model.User;
import com.secdev.project.model.Asset;
import com.secdev.project.repo.UserRepository;
import com.secdev.project.repo.AssetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@Profile("dev") 
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               AssetRepository assetRepository,
                               PasswordEncoder passwordEncoder) {

        return args -> {

            User admin = createUserIfNotExists(
                    userRepository, passwordEncoder,
                    "admin@secdev.com",
                    "System Administrator",
                    "+63912345678",
                    "Admin123!",
                    Role.ADMIN
            );

            User alice = createUserIfNotExists(
                    userRepository, passwordEncoder,
                    "alice@example.com",
                    "Alice Reyes",
                    "+639111111111",
                    "User123!",
                    Role.USER
            );

            User bob = createUserIfNotExists(
                    userRepository, passwordEncoder,
                    "bob@example.com",
                    "Bob Santos",
                    "+639222222222",
                    "User123!",
                    Role.USER
            );

            User carla = createUserIfNotExists(
                    userRepository, passwordEncoder,
                    "carla@example.com",
                    "Carla Cruz",
                    "+639333333333",
                    "User123!",
                    Role.USER
            );

            createAssetIfNotExists(assetRepository, alice, "Laptop", 45000.0, 1);
            createAssetIfNotExists(assetRepository, alice, "Mouse", 800.0, 2);

            createAssetIfNotExists(assetRepository, bob, "Monitor", 12000.0, 1);
            createAssetIfNotExists(assetRepository, carla, "Keyboard", 1500.0, 1);
        };
    }

    private User createUserIfNotExists(UserRepository repo,
                                       PasswordEncoder encoder,
                                       String email,
                                       String name,
                                       String phone,
                                       String password,
                                       Role role) {

        return repo.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email.toLowerCase().trim());
            user.setFullName(name);
            user.setPhoneNumber(phone);
            user.setPassword(encoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);
            user.setAccountNonLocked(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return repo.save(user);
        });
    }

    private void createAssetIfNotExists(AssetRepository repo,
                                        User owner,
                                        String name,
                                        Double value,
                                        Integer quantity) {

        boolean exists = repo.findByOwnerEmail(owner.getEmail()).stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(name));

        if (exists) return;

        Asset asset = new Asset();
        asset.setName(name);
        asset.setValue(value);
        asset.setQuantity(quantity);
        asset.setOwner(owner);
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());

        repo.save(asset);
    }
}