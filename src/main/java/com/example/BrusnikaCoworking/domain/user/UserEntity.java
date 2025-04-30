package com.example.BrusnikaCoworking.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Setter
@Getter
@Entity
@Table(name = "Users")
@FieldNameConstants
@NoArgsConstructor
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "realname", nullable = false)
    private String realname;
    @Column(name = "password", nullable = false)
    private String password = "keylock";
    @Column(name = "count_block", nullable = false)
    private Integer countBlock;
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status_block", nullable = false)
//    private StatusBlock statusBlock;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return countBlock < 5 ? true : false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
