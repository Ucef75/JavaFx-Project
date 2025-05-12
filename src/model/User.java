package model;

public class User {
    private Integer userId;
    private String username;
    private String password;
    private String country;
    private String created;   // date de création du compte (format : yyyy-MM-dd)
    private String birthdate; // date de naissance (format : yyyy-MM-dd)

    public User(Integer userId, String username, String password, String country, String created, String birthdate) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.country = country;
        this.created = created;
        this.birthdate = birthdate;
    }
    // Getters
    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCountry() { return country; }
    public String getCreated() { return created; }
    public String getBirthdate() { return birthdate; }


    // Optionnels : Setters si tu veux permettre la modification après création
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCountry(String country) { this.country = country; }
    public void setCreated(String created) { this.created = created; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
}
