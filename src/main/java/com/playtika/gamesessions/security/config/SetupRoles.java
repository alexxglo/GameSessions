package com.playtika.gamesessions.security.config;

import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.RoleType;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SetupRoles implements ApplicationListener<ContextRefreshedEvent> {

    boolean setupComplete = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(setupComplete)
            return;

        // == create initial roles
        final Role adminRole = createRoleIfNotFound(RoleType.ROLE_ADMIN.name());
        final Role userRole = createRoleIfNotFound(RoleType.ROLE_USER.name());
        final Role managerRole = createRoleIfNotFound(RoleType.ROLE_MANAGER.name());
        // == create initial user
        createUserIfNotFound("admin@test.com", "admin", "Admin",
                "Admin", "1234", new ArrayList<>(Arrays.asList(adminRole)), 100);

        createUserIfNotFound("admina@test.com", "admina", "Admina",
                "Admina", "12334", new ArrayList<>(Arrays.asList(adminRole)), 200);

        createUserIfNotFound("tester@test.com", "tester", "Tester",
                "Tester", "12313145", new ArrayList<>(Arrays.asList(userRole)), 300);
        createUserIfNotFound("qa@test.com", "qa", "QA",
                "QA", "1111", new ArrayList<>(Arrays.asList(userRole)), 250);
        createUserIfNotFound("hr@test.com", "hr", "HR",
                "HR", "hrhr", new ArrayList<>(Arrays.asList(managerRole)), 30);
        createUserIfNotFound("po@test.com", "PO", "PO",
                "PO", "popo", new ArrayList<>(Arrays.asList(managerRole)), 75);
        createUserIfNotFound("scrum@test.com", "scrum", "scrum",
                "scrum", "scrumscrum", new ArrayList<>(Arrays.asList(userRole)), 90);

        setupComplete = true;
    }

    @Transactional
    Role createRoleIfNotFound(String name) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
        }
        role = roleRepository.save(role);
        return role;
    }

    @Transactional
    User createUserIfNotFound(final String email, final String username, final String firstName, final String lastName, final String password, final List<Role> roles,
                              int maxDailyTime) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setMaxDailyTime(maxDailyTime);
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        return user;
    }
}
