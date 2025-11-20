package com.app_odontologia.diplomado_final.model.entity;

import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter
@ToString(exclude = {"clinic", "createdBy"})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean mustChangePassword = false;

    private String nombres;
    private String apellidos;
    private String ocupacion;
    private String zona;
    private String direccion;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.PENDING_ACTIVATION;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // Relacion con Clinica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinica_id")
    private Clinic clinic;

    // Relacion con usuario Creador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id")
    private User createdBy;

    @Column(nullable = false)
    private Boolean mustCompleteProfile = false;

    // Si deseas almacenar especialidad y matricula:
    private String especialidad;
    private String matricula;
    private String telefonoClinico;
    private String bioClinica;

    // helper
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.getName().equals(roleName));
    }

    // Métodos de UserDetails:
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return status != UserStatus.BLOCKED; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
