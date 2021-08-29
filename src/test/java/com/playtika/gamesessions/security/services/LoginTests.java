package com.playtika.gamesessions.security.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtika.gamesessions.exceptions.MyCustomException;
import com.playtika.gamesessions.security.dto.LoginResponse;
import com.playtika.gamesessions.security.dto.PatchUser;
import com.playtika.gamesessions.security.dto.SignUpRequest;
import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.repositories.UserRepository;
import com.playtika.gamesessions.security.services.UserService;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RunWith(MockitoJUnitRunner.class)
public class LoginTests {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Authentication auth = mock(Authentication.class);

    private AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginResponse loginResponse;

    private User userMockNoData = mock(User.class);

    private SignUpRequest requestMockNoData = mock(SignUpRequest.class);


    public User configureUserMock() {
        User mockedUser = mock(User.class);
        Role role;
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");
        roles.add(role);

        when(mockedUser.getUsername()).thenReturn("username");
        when(mockedUser.getPassword()).thenReturn("pwd");
        when(mockedUser.getRoles()).thenReturn(roles);


        return mockedUser;
    }

    public SignUpRequest configureSignUpRequest() {
        SignUpRequest request = mock(SignUpRequest.class);
        Role role = mock(Role.class);
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");

        when(request.getUserName()).thenReturn("requestName");
        when(request.getPassword()).thenReturn("requestPwd");
        when(request.getEmail()).thenReturn("requestEmail");
        when(request.getFirstName()).thenReturn("requestFirstName");
        when(request.getLastName()).thenReturn("requestLastName");

        return request;
    }
    @Test
    public void testGoodFlowOnLogin() {
        //GIVEN
        User userMock = configureUserMock();
        //WHEN
        when(userRepository.findByUsername(userMock.getUsername())).thenReturn(userMock);
        when(jwtTokenService.createToken(userMock.getUsername(), userMock.getRoles())).thenReturn("OK");

        //THEN
        assertThat(userMock.getEmail()).isSameAs(userService.login(userMock.getUsername(),userMock.getPassword()).getEmail());
    }

    @Test
    public void testNullValuesOnLoginTest() {
        User userMock = configureUserMock();
        when(userRepository.findByUsername(userMock.getUsername())).thenReturn(userMock);
        when(jwtTokenService.createToken(userMock.getUsername(), userMock.getRoles())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> userService.login(userMock.getUsername(), userMock.getPassword())).isInstanceOf(Exception.class);
    }

    @Test
    public void loadUserByUsernameTest() {
        User user = configureUserMock();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        assertThat(userService.loadUserByUsername(user.getUsername()).getUsername()).isSameAs(user.getUsername());
    }

    @Test
    public void nullUserLoadUserTest() {
        User userMock = configureUserMock();
        assertThatThrownBy(() -> userService.loadUserByUsername(userMock.getUsername())).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void goodFlowSignUpTest() {

        SignUpRequest request = configureSignUpRequest();

        when(userRepository.existsByUsername(request.getUserName())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(null);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encryptedPassword");

        assertThat(request.getUserName()).isEqualTo(userService.signUp(request).getUsername());
    }

    @Test
    public void checkExceptionSignUpTest() {

        SignUpRequest request = configureSignUpRequest();

        when(userRepository.existsByUsername(request.getUserName())).thenReturn(true);

        assertThatThrownBy(() -> userService.signUp(request)).isInstanceOf(MyCustomException.class);
    }

    @Test
    public void inexistentUserRemoveUserTest() {
        User user = configureUserMock();
        String requesterUsername = "req";

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);

        assertThatThrownBy(() -> userService.removeUser(user.getUsername(), requesterUsername)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void goodFlowRemoveUserTest() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("testpwd");
        user.setEmail("email");
        Role role;
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");
        roles.add(role);
        user.setRoles(roles);
        String requesterUsername = "req";

        when(user.getRoles().get(0).getName()).thenReturn("ROLE_ADMIN");
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(userRepository.findByUsername(requesterUsername)).thenReturn(user);

        userService.removeUser(user.getUsername(),requesterUsername);

    }

    @Test
    public void notFoundUserSearchUserTest() {
        User user = configureUserMock();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);

        assertThatThrownBy(() -> userService.searchUser(user.getUsername())).isInstanceOf(MyCustomException.class);
    }

    @Test
    public void foundUserSearchUserTest() {
        User user = configureUserMock();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        assertThat(userService.searchUser(user.getUsername()).getUserName()).isEqualTo(user.getUsername());
    }

    @Test
    public void getAllUsersTest() {
        User user = configureUserMock();
        List<User> userList = new ArrayList<>();
        userList.add(user);
        when(userRepository.findAll()).thenReturn(userList);

        assertThat(userService.getAllUser()).isEqualTo(userList);
    }

    @Test
    public void refreshTokenTest() {
        User user = configureUserMock();
        String responseToken = "token";
        Role role;
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");
        roles.add(role);


        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(userRepository.findByUsername(user.getUsername()).getRoles()).thenReturn(roles);
        when(jwtTokenService.createToken(user.getUsername(), userRepository.findByUsername(user.getUsername()).getRoles())).thenReturn(responseToken);

        assertThat(userService.refreshToken(user.getUsername())).isEqualTo(responseToken);
    }


    @Test
    public void updateUserSelfTest() {
        User user = configureUserMock();
        PatchUser patchUser = mock(PatchUser.class);
        Role role;
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");
        roles.add(role);

        when(patchUser.getUserName()).thenReturn("username");
        when(patchUser.getEmail()).thenReturn("email");
        when(patchUser.getPassword()).thenReturn("password");
        when(patchUser.getRoles()).thenReturn(roles);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(userRepository.saveAndFlush(any())).thenReturn(user);

        assertThat(userService.updateUserSelf(patchUser, user.getUsername()).getUsername()).isEqualTo(user.getUsername());
    }

    @Test

    public void updateUserByIdTest() {
        User user = configureUserMock();
        PatchUser patchUser = mock(PatchUser.class);
        Optional<User> userOpt = Optional.of(user);
        long id = 5;
        Role role;
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");
        roles.add(role);

        when(patchUser.getRoles()).thenReturn(roles);
        when(userRepository.findById(id)).thenReturn(userOpt);
        when(roleRepository.findByName(patchUser.getRoles().get(0).getName())).thenReturn(role);
        when(userRepository.getById(id)).thenReturn(user);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(userRepository.saveAndFlush(any())).thenReturn(user);

        assertThat(userService.updateUserById(patchUser,id,user.getUsername()).getEmail()).isEqualTo(user.getEmail());
    }
}
