package com.minimarket.dto;

import com.minimarket.entity.Usuario;
import org.springframework.hateoas.server.core.Relation;

import java.util.Set;
import java.util.stream.Collectors;

@Relation(collectionRelation = "usuarios", itemRelation = "usuario")
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private Set<String> roles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public static UsuarioResponseDTO from(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        if (usuario.getRoles() != null) {
            dto.setRoles(usuario.getRoles().stream()
                    .map(rol -> rol.getNombre())
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}
