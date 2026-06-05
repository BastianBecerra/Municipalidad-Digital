package muni.usuarios.services;

import muni.usuarios.controller.dto.AuthResponse;
import muni.usuarios.controller.dto.LoginRequest;
import muni.usuarios.controller.dto.RegisterRequest;
import muni.usuarios.entities.Territorio;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.TerritorioRepository;
import muni.usuarios.repository.UsuarioRepository;
import muni.usuarios.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TerritorioRepository territorioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLogin_Success_WithRut() {
        LoginRequest req = new LoginRequest();
        req.setRut("12345678-9");
        req.setPassword("pass");

        Usuarios u = new Usuarios();
        u.setRut("12345678-9");

        when(usuarioRepository.findByRut("12345678-9")).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("mock_token");

        AuthResponse res = authService.login(req);
        assertThat(res.getToken()).isEqualTo("mock_token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLogin_Success_WithEmail() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("pass");

        Usuarios u = new Usuarios();
        u.setEmail("user@example.com");

        when(usuarioRepository.findByRut("user@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("mock_token");

        AuthResponse res = authService.login(req);
        assertThat(res.getToken()).isEqualTo("mock_token");
    }

    @Test
    void testLogin_UsernameNullOrEmpty() {
        LoginRequest req = new LoginRequest();
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Debe proporcionar un RUT o un Email");
    }

    @Test
    void testLogin_UserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setRut("123");
        req.setPassword("pass");

        when(usuarioRepository.findByRut("123")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void testRegister_Success_NoTerritory() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("123");
        req.setEmail("u@e.com");
        req.setPassword("pass");

        when(usuarioRepository.existsByRut("123")).thenReturn(false);
        when(usuarioRepository.existsByEmail("u@e.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
        when(jwtService.generateToken(any(Usuarios.class))).thenReturn("token");

        AuthResponse res = authService.register(req);
        assertThat(res.getToken()).isEqualTo("token");
        verify(usuarioRepository).save(any(Usuarios.class));
    }

    @Test
    void testRegister_Success_WithTerritory() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("123");
        req.setEmail("u@e.com");
        req.setPassword("pass");
        req.setPasswordClaveUnica("cu");
        req.setTerritorioId(1L);

        Territorio t = new Territorio();
        when(usuarioRepository.existsByRut("123")).thenReturn(false);
        when(usuarioRepository.existsByEmail("u@e.com")).thenReturn(false);
        when(territorioRepository.findById(1L)).thenReturn(Optional.of(t));
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
        when(passwordEncoder.encode("cu")).thenReturn("encoded_cu");
        when(jwtService.generateToken(any(Usuarios.class))).thenReturn("token");

        AuthResponse res = authService.register(req);
        assertThat(res.getToken()).isEqualTo("token");
    }

    @Test
    void testRegister_RutExists() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("123");
        when(usuarioRepository.existsByRut("123")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un usuario con el RUT");
    }

    @Test
    void testRegister_EmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("123");
        req.setEmail("u@e.com");
        when(usuarioRepository.existsByRut("123")).thenReturn(false);
        when(usuarioRepository.existsByEmail("u@e.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un usuario con el email");
    }

    @Test
    void testRegister_TerritoryNotFound() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("123");
        req.setEmail("u@e.com");
        req.setTerritorioId(1L);

        when(usuarioRepository.existsByRut("123")).thenReturn(false);
        when(usuarioRepository.existsByEmail("u@e.com")).thenReturn(false);
        when(territorioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Territorio no encontrado");
    }

    @Test
    void testLogin_RutEmpty_FallbackToEmail() {
        LoginRequest req = new LoginRequest();
        req.setRut("");
        req.setEmail("fallback@example.com");
        req.setPassword("pass");

        Usuarios u = new Usuarios();
        u.setEmail("fallback@example.com");

        when(usuarioRepository.findByRut("fallback@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("fallback@example.com")).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("token");

        AuthResponse res = authService.login(req);
        assertThat(res.getToken()).isEqualTo("token");
    }

    @Test
    void testLogin_UsernameEmptyString() {
        LoginRequest req = new LoginRequest();
        req.setRut("");
        req.setEmail("");
        req.setPassword("pass");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Debe proporcionar un RUT o un Email");
    }

    @Test
    void testRegister_WithRol() {
        RegisterRequest req = new RegisterRequest();
        req.setRut("123");
        req.setEmail("u@e.com");
        req.setPassword("pass");
        req.setRol("ADMIN");

        when(usuarioRepository.existsByRut("123")).thenReturn(false);
        when(usuarioRepository.existsByEmail("u@e.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
        when(jwtService.generateToken(any(Usuarios.class))).thenReturn("token");

        AuthResponse res = authService.register(req);
        assertThat(res.getToken()).isEqualTo("token");
        
        org.mockito.ArgumentCaptor<Usuarios> captor = org.mockito.ArgumentCaptor.forClass(Usuarios.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getRol()).isEqualTo("ADMIN");
    }
}
