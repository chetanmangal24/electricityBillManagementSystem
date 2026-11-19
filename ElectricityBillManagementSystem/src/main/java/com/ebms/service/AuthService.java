package com.ebms.service;

import com.ebms.dao.UserDAO;
import com.ebms.model.User;
import com.ebms.util.PasswordUtil;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Service for authentication operations.
 */
public class AuthService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate user with username and password.
     */
    public boolean login(String username, String password) throws SQLException {
        Optional<User> userOpt = userDAO.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                this.currentUser = user;
                return true;
            }
        }

        return false;
    }

    /**
     * Logout current user.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Get currently logged in user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
