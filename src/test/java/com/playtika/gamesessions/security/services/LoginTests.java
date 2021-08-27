package com.playtika.gamesessions.security.services;

import com.playtika.gamesessions.exceptions.MyCustomException;
import com.playtika.gamesessions.security.services.UserService;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@RunWith(JUnitParamsRunner.class)
public class LoginTests {

    @Test
    @Parameters("notindatabase,null")
    public void testNullValuesOnLogin(@Nullable String username, String password) {
        Authentication auth = mock(Authentication.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        UserService userService = mock(UserService.class);
        Mockito.when(authenticationManager.authenticate(auth)).thenThrow(MyCustomException.class);
        assertThatThrownBy(() -> userService.login(username, password)).isInstanceOf(MyCustomException.class);
    }
}
