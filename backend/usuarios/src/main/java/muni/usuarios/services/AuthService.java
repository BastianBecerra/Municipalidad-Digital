package muni.usuarios.services;

import lombok.RequiredArgsConstructor;
import muni.usuarios.controller.dto.AuthResponse;
import muni.usuarios.controller.dto.LoginRequest;
import muni.usuarios.controller.dto.RegisterRequest;
import muni.usuarios.entities.Territorio;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.TerritorioRepository;
import muni.usuarios.repository.UsuarioRepository;
import muni.usuarios.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TerritorioRepository territorioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        String username = (request.getRut() != null && !request.getRut().isEmpty()) ? request.getRut() : request.getEmail();

        if (username == null || username.isEmpty()) {
            throw new RuntimeException("Debe proporcionar un RUT o un Email para iniciar sesión");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        request.getPassword()
                )
        );

        Usuarios usuario = usuarioRepository.findByRut(username)
                .orElseGet(() -> usuarioRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado con RUT o Email: " + username)));

        String jwtToken = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByRut(request.getRut())) {
            throw new RuntimeException("Ya existe un usuario con el RUT: " + request.getRut());
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con el email: " + request.getEmail());
        }

        Territorio territorio = null;
        if (request.getTerritorioId() != null) {
            territorio = territorioRepository.findById(request.getTerritorioId())
                    .orElseThrow(() -> new RuntimeException("Territorio no encontrado con ID: " + request.getTerritorioId()));
        }

        Usuarios usuario = Usuarios.builder()
                .nombres(request.getNombres())
                .apellidoPaterno(request.getApellidoPaterno())
                .apellidoMaterno(request.getApellidoMaterno())
                .rut(request.getRut())
                .fechaNacimiento(request.getFechaNacimiento())
                .genero(request.getGenero())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .comuna(request.getComuna())
                .region(request.getRegion())
                .password(passwordEncoder.encode(request.getPassword()))
                .passwordClaveUnica(request.getPasswordClaveUnica() != null ? passwordEncoder.encode(request.getPasswordClaveUnica()) : null)
                .rol(request.getRol() == null ? "VECINO" : request.getRol())
                .territorio(territorio)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);
        String jwtToken = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
