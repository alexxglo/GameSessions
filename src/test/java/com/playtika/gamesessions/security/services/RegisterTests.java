package com.playtika.gamesessions.security.services;

import com.playtika.gamesessions.exceptions.MyCustomException;
import com.playtika.gamesessions.security.dto.SignUpRequest;
import com.playtika.gamesessions.security.services.UserService;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import junitparams.JUnitParamsRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class RegisterTests {

    @Test
    public void testSignUpWithInvalidValues() {
        UserService userService = mock(UserService.class);
        SignUpRequest signUpRequest = mock(SignUpRequest.class);
        assertThatThrownBy(() -> userService.signUp(signUpRequest)).isInstanceOf(MyCustomException.class);
    }
}
