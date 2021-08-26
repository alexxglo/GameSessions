package com.playtika.gamesessions.security.services;

import com.playtika.gamesessions.exceptions.MyCustomException;
import com.playtika.gamesessions.security.dto.LoginResponse;
import com.playtika.gamesessions.security.dto.PatchUser;
import com.playtika.gamesessions.security.dto.SignUpRequest;
import com.playtika.gamesessions.security.dto.UserDTO;
import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.RoleType;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.hibernate.NonUniqueObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RoleRepository roleRepository;

    //required by the UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        final User user = userRepository.findByUsername(userName);

        if (user == null) {
            throw new UsernameNotFoundException("User '" + userName + "' not found");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(userName)
                .password(user.getPassword())
                .authorities(user.getRoles())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }


    private List<Role> initialUserRole() {
        Role role = roleRepository.findByName(RoleType.ROLE_USER.toString());
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        return roles;
    }

    public LoginResponse login(String userName, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));

            User user = userRepository.findByUsername(userName);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setEmail(user.getEmail());
            loginResponse.setUserName(user.getUsername());
            loginResponse.setAccessToken(jwtTokenService.createToken(userName, user.getRoles()));

            logger.info("Login successfully");

            return loginResponse;
        } catch (AuthenticationException e) {
            throw new MyCustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public User signUp(SignUpRequest request) {
        if(userRepository.existsByUsername(request.getUserName())){
            throw new MyCustomException("User already exists in system", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User user = new User();
        user.setUsername(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoles(initialUserRole());
        request.setPassword(user.getPassword());

        userRepository.save(user);
        logger.info("Register successfully");

        return user;
    }


    public void removeUser(String userName) {
        if(!userRepository.existsByUsername(userName)){
            throw new RuntimeException("User doesn't exists");
        }
        userRepository.deleteByUsername(userName);
        logger.info("User remove successfully");

    }

    public UserDTO searchUser(String userName) {
        User user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new MyCustomException("Provided user doesn't exist", HttpStatus.NOT_FOUND);
        }
        UserDTO userResponse = new UserDTO(user.getUsername(), user.getEmail());

        return userResponse;
    }

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public String refreshToken(String userName) {
        return jwtTokenService.createToken(userName, userRepository.findByUsername(userName).getRoles());
    }

    public User getUserById(long id) {
        return userRepository.getById(id);
    }

    private User updateUser(PatchUser patchUser, User userToUpdate) {

        if(patchUser.getEmail() != null) {
            userToUpdate.setEmail(patchUser.getEmail());
        }
        if(patchUser.getPassword() != null) {
            userToUpdate.setPassword(patchUser.getPassword());
        }
        if(patchUser.getUserName() != null) {
            userToUpdate.setUsername(patchUser.getUserName());
        }
        if(patchUser.getRoles() != null) {
            System.out.println(patchUser.getRoles().get(0).getName());
            userToUpdate.setRoles(patchUser.getRoles());
        }
        return userToUpdate;
    }
    private boolean verifyRoleLevel(User userToUpdate, User requestUser) {
        RoleType updatedUserRole = RoleType.stringToRoleType(userToUpdate.getRoles().get(0).getName());
        RoleType requestUserRole = RoleType.stringToRoleType(requestUser.getRoles().get(0).getName());
        boolean equalRoleLevels = requestUserRole.getRoleLevel() == updatedUserRole.getRoleLevel();
        boolean roleIsAdmin = RoleType.ROLE_ADMIN.toString().equals(RoleType.RoleTypeToString(requestUserRole));
        System.out.println("Equal levels" + equalRoleLevels);
        System.out.println("Role is admin " + roleIsAdmin);
        if(requestUserRole.getRoleLevel() > updatedUserRole.getRoleLevel()) {
            return true;
        }
        else if(equalRoleLevels && roleIsAdmin) {
            System.out.println("yes");
            return true;
        }
        return false;
    }

    public User updateUserById(PatchUser patchUser, long id, String username) { //TODO add update roles
        Optional<User> user = userRepository.findById(id);
        System.out.println("User that updates another user: " + username);
        if(user.isPresent()) {
            User userToUpdate = userRepository.getById(id);
            User requestUser = userRepository.findByUsername(username);
            if(verifyRoleLevel(userToUpdate, requestUser)) {
                User userUpdated = updateUser(patchUser, userToUpdate);
                try {
                    return userRepository.saveAndFlush(userUpdated);
                } catch (Exception e) {
                    System.out.println("Shit");
                    return null;
                }
            }
        }
        return null;
    }

    public User updateUserSelf(PatchUser patchUser, String username) {
        User userToUpdate = userRepository.findByUsername(username);
        User userUpdated = updateUser(patchUser, userToUpdate);
        try {
            return userRepository.saveAndFlush(userUpdated);
        }
        catch (Exception e) {
            return null;
        }
    }
}
