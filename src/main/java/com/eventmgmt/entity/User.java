package com.eventmgmt.entity;

import com.eventmgmt.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents every person in the system — Admin, Organizer, or Student.
 * Implements UserDetails so Spring Security can use it directly
 * without a separate wrapper class.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --------------------------------------------------------
    // Relationships (mapped here for navigation — lazy loaded)
    // --------------------------------------------------------

    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Event> organizedEvents = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();

    // --------------------------------------------------------
    // JPA Lifecycle Hooks
    // --------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // UserDetails implementation
    // Spring Security calls these methods during authentication
    // --------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security convention: roles are prefixed with "ROLE_"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // We use email as the unique login identifier
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}