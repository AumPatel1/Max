package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Users")
@Data
@NoArgsConstructor
@AllArgsConstructor@Builder

public class UserEntity
{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="userId")
    private Long userId;

   @Column(name = "username",nullable = false,unique = true)
    private String username;

   @Column(name ="password",nullable = false)
    private String password;

   @Column(name = "role")
    private String role;
}
