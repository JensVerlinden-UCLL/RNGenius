package be.ucll.mobile.rngenius.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "users")
public class User {

    @Id
    public long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email value is invalid, it has to be of the following format xxx@yyy.zzz")
    private String email;

    private String password;

    private String refreshToken;

    public User(String firstName, String lastName, String email, String password) throws UserException {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        setPassword(password);
    }

    public User() {}


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonIgnore    
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setPassword(String password) throws UserException {
        if (password == null || password.strip() == "") {
            throw new UserException("password", "Password is required");
        }

        if (password.length() < 8) {
            throw new UserException("password", "Password is too short, it has to be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new UserException("password", "Password has to contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new UserException("password", "Password has to contain at least one lowercase letter");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new UserException("password", "Password has to contain at least one number");
        }

        if (!password.matches(".*[!@?#$%^&*].*")) {
            throw new UserException("password", "Password has to contain at least one special character");
        }

        this.password = password;
        
    }

    public void setPasswordBcrypt(String password) {
        this.password = password;
    }

    public void setRefreshToken(String refreshToken) throws UserException {
        if (refreshToken == null || refreshToken.strip() == "") {
            throw new UserException("refreshToken", "Refresh token is required");
        }

        this.refreshToken = refreshToken;
    }
}