package be.ucll.mobile.rngenius.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import be.ucll.mobile.rngenius.user.model.User;
import be.ucll.mobile.rngenius.user.repo.UserRepository;
import be.ucll.mobile.rngenius.user.service.UserService;
import be.ucll.mobile.rngenius.user.service.UserServiceException;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    UserService userService;

    private User user;

    @BeforeEach
    public void setUp()  throws Exception {
        user = new User("John", "Doe", "john.doe@ucll.be", "JohnD123!");
        user.id = (1L);
    }   

    @Test
    void givenValidUserData_whenCreatingUser_thenUserCreatedSuccessfullyAndReturned() throws Exception {
        // given
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(null);
        when(bCryptPasswordEncoder.encode(any(String.class))).thenReturn("SECRET");
        when(userRepository.save(user)).thenReturn(user);

        // when
        userService.addUser(user);

        // then
        assertNotNull(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenNullUserData_whenCreatingUser_thenUserServiceExceptionIsThrown() {
        // given
        // when
        UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.addUser(null));
        // then
        assertEquals("user", ex.getField());
        assertEquals("User data is required", ex.getMessage());
    }

    @Test
    void givenExistingEmail_whenCreatingUser_thenUserServiceExceptionIsThrown() {
        // given
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);

        // when
        UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.addUser(user));

        // then
        assertEquals("user", ex.getField());
        assertEquals("User with this email already exists", ex.getMessage());
    }

    @Test
    void givenValidEmail_whenGettingUserByEmail_thenUserReturned() throws UserServiceException {
        // given
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(user);

        // when
        User foundUser = userService.getUserByEmail(user.getEmail());

        // then
        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void givenInvalidEmail_whenGettingUserByEmail_thenUserServiceExceptionIsThrown() {
        // given
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(null);

        // when
        UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.getUserByEmail(user.getEmail()));

        // then
        assertEquals("user", ex.getField());
        assertEquals("No user with this email", ex.getMessage());
    }

    @Test
    void givenValidId_whenGettingUserById_thenUserReturned() throws UserServiceException {
        // given
        when(userRepository.findUserById(user.id)).thenReturn(user);

        // when
        User foundUser = userService.getUserById(user.id);

        // then
        assertNotNull(foundUser);
        assertEquals(user.id, foundUser.id);
    }

    @Test
    void givenInvalidId_whenGettingUserById_thenUserServiceExceptionIsThrown() {
        // given
        when(userRepository.findUserById(user.id)).thenReturn(null);

        // when
        UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.getUserById(user.id));

        // then
        assertEquals("user", ex.getField());
        assertEquals("No user with this id", ex.getMessage());
    }

    @Test
    void givenValidUser_whenSettingRefreshTokenOnLogin_thenRefreshTokenSet() throws Exception {
        // given
        String refreshToken = UUID.randomUUID().toString();
        when(bCryptPasswordEncoder.encode(any(String.class))).thenReturn(refreshToken);
        when(userRepository.save(user)).thenReturn(user);

        // when
        User updatedUser = userService.setRefreshTokenOnLogin(user);

        // then
        assertNotNull(updatedUser.getRefreshToken());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenValidRefreshToken_whenCheckingRefreshToken_thenUserReturned() throws Exception {
        // given
        String refreshToken = UUID.randomUUID().toString();
        when(userRepository.findUserById(user.id)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(refreshToken, user.getRefreshToken())).thenReturn(true);

        // when
        User foundUser = userService.checkRefreshToken(user.id, refreshToken);

        // then
        assertNotNull(foundUser);
        assertEquals(user.id, foundUser.id);
    }

    @Test
    void givenInvalidRefreshToken_whenCheckingRefreshToken_thenUserServiceExceptionIsThrown() throws Exception {
        // given
        when(userRepository.findUserById(user.id)).thenReturn(user);
        when(bCryptPasswordEncoder.matches("invalidToken", user.getRefreshToken())).thenReturn(false);

        // when
        UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.checkRefreshToken(user.id, "invalidToken"));

        // then
        assertEquals("user", ex.getField());
        assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    void givenValidOldPassword_whenChangingPassword_thenPasswordChangedSuccessfully() throws Exception {
        // given
        String oldPassword = "JohnD123!";
        String newPassword = "NewPassword123!";
        when(userRepository.findUserById(user.id)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(bCryptPasswordEncoder.encode(newPassword)).thenReturn("ENCODED_NEW_PASSWORD");

        // when
        userService.changePassword(user.id, oldPassword, newPassword);

        // then
        verify(userRepository, times(1)).save(user);
        assertEquals("ENCODED_NEW_PASSWORD", user.getPassword());
    }

    @Test
    void givenInvalidOldPassword_whenChangingPassword_thenUserServiceExceptionIsThrown() {
        // given
        String oldPassword = "WrongPassword";
        String newPassword = "NewPassword123!";
        when(userRepository.findUserById(user.id)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        // when
        UserServiceException ex = assertThrows(UserServiceException.class, () -> userService.changePassword(user.id, oldPassword, newPassword));

        // then
        assertEquals("user", ex.getField());
        assertEquals("Invalid password", ex.getMessage());
    }
}