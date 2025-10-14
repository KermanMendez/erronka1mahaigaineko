package model;

import java.io.Serializable;

public class PojoRegistratu implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String email;
    private String password;

    public PojoRegistratu(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
