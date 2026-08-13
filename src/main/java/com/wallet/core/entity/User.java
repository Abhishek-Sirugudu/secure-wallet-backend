package com.wallet.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //exactly one account for one user
    //user keyowrd must be same as in the account table
    //cascade means if you delete the user from the database,
    // it automatically deletes their account row too,
    // or if you create the user, it creates the account row.
    private Account account;

    @PrePersist
    protected void OnCreate(){
        createdAt = LocalDateTime.now();
    }


}
