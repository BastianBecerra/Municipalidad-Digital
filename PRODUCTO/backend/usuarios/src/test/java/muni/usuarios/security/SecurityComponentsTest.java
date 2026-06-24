package muni.usuarios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityComponentsTest {

    private JwtService jwtService;
    private final String secretKey = "bXVuaS1kaWdpdGFsLXNlY3JldC1rZXktMjAyNC1zZWN1cmUtand0LXRva2Vu";

    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomAuthenticationProvider customAuthenticationProvider;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "jwtService", jwtService);

        customAuthenticationProvider = new CustomAuthenticationProvider(usuarioRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testJwtService_GenerateAndExtractToken() {
        Usuarios u = new Usuarios();
        u.setRut("12345678-9");
        u.setRol("ADMIN");

        String token = jwtService.generateToken(u);
        assertThat(token).isNotEmpty();

        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("12345678-9");

        boolean isValid = jwtService.isTokenValid(token, u);
        assertThat(isValid).isTrue();
    }

    @Test
    void testJwtService_TokenExpiredSpy() {
        JwtService spyService = spy(jwtService);
        doReturn(true).when(spyService).isTokenExpired(anyString());

        Usuarios u = new Usuarios();
        u.setRut("123");
        u.setRol("ADMIN");
        String token = jwtService.generateToken(u);
        
        boolean isValid = spyService.isTokenValid(token, u);
        assertThat(isValid).isFalse();
    }

    @Test
    void testFilter_NoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testFilter_InvalidPrefix() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Token xyz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testFilter_ValidToken_Success() throws Exception {
        Usuarios u = new Usuarios();
        u.setRut("12345678-9");
        u.setRol("ADMIN");

        String token = jwtService.generateToken(u);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userDetailsService.loadUserByUsername("12345678-9")).thenReturn(u);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("12345678-9");
    }

    @Test
    void testFilter_TokenInvalid() throws Exception {
        Usuarios u = new Usuarios();
        u.setRut("12345678-9");

        String token = jwtService.generateToken(u);

        Usuarios otherUser = new Usuarios();
        otherUser.setRut("different_rut");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(userDetailsService.loadUserByUsername("12345678-9")).thenReturn(otherUser);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testFilter_AlreadyAuthenticated() throws Exception {
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                "existing", null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        Usuarios u = new Usuarios();
        u.setRut("123");
        u.setRol("ADMIN");
        String token = jwtService.generateToken(u);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void testAuthenticate_Success_Email() {
        Usuarios u = new Usuarios();
        u.setRut("123");
        u.setEmail("user@example.com");
        u.setPassword("encoded_pass");
        u.setRol("ADMIN");
        u.setActivo(true);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("raw_pass", "encoded_pass")).thenReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", "raw_pass");
        Authentication result = customAuthenticationProvider.authenticate(auth);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("123");
    }

    @Test
    void testAuthenticate_Success_Rut() {
        Usuarios u = new Usuarios();
        u.setRut("12345678-9");
        u.setPasswordClaveUnica("encoded_cu");
        u.setRol("ADMIN");
        u.setActivo(true);

        when(usuarioRepository.findByRut("12.345.678-9")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("raw_cu", "encoded_cu")).thenReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("12345678-9", "raw_cu");
        Authentication result = customAuthenticationProvider.authenticate(auth);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("12345678-9");
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(usuarioRepository.findByRut("12-3")).thenReturn(Optional.empty());

        Authentication auth = new UsernamePasswordAuthenticationToken("123", "pass");
        assertThatThrownBy(() -> customAuthenticationProvider.authenticate(auth))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void testAuthenticate_WrongPassword_Email() {
        Usuarios u = new Usuarios();
        u.setEmail("user@example.com");
        u.setPassword("encoded_pass");

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrong_pass", "encoded_pass")).thenReturn(false);

        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", "wrong_pass");
        assertThatThrownBy(() -> customAuthenticationProvider.authenticate(auth))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Contraseña incorrecta");
    }

    @Test
    void testAuthenticate_NullPassword_Email() {
        Usuarios u = new Usuarios();
        u.setEmail("user@example.com");
        u.setPassword(null);

        when(usuarioRepository.findByEmail("user@example.com")).thenReturn(Optional.of(u));

        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", "pass");
        assertThatThrownBy(() -> customAuthenticationProvider.authenticate(auth))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Contraseña incorrecta");
    }

    @Test
    void testAuthenticate_WrongPassword_Rut() {
        Usuarios u = new Usuarios();
        u.setRut("123");
        u.setPasswordClaveUnica("encoded_cu");

        when(usuarioRepository.findByRut("12-3")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrong_cu", "encoded_cu")).thenReturn(false);

        Authentication auth = new UsernamePasswordAuthenticationToken("123", "wrong_cu");
        assertThatThrownBy(() -> customAuthenticationProvider.authenticate(auth))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Contraseña incorrecta");
    }

    @Test
    void testAuthenticate_NullPassword_Rut() {
        Usuarios u = new Usuarios();
        u.setRut("123");
        u.setPasswordClaveUnica(null);

        when(usuarioRepository.findByRut("12-3")).thenReturn(Optional.of(u));

        Authentication auth = new UsernamePasswordAuthenticationToken("123", "pass");
        assertThatThrownBy(() -> customAuthenticationProvider.authenticate(auth))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Contraseña incorrecta");
    }

    @Test
    void testAuthenticate_InactiveUser() {
        Usuarios u = new Usuarios();
        u.setRut("123");
        u.setPasswordClaveUnica("encoded_cu");
        u.setActivo(false);

        when(usuarioRepository.findByRut("12-3")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("raw_cu", "encoded_cu")).thenReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("123", "raw_cu");
        assertThatThrownBy(() -> customAuthenticationProvider.authenticate(auth))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("El usuario está desactivado");
    }

    @Test
    void testSupports() {
        boolean supports = customAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class);
        assertThat(supports).isTrue();

        boolean supportsOther = customAuthenticationProvider.supports(String.class);
        assertThat(supportsOther).isFalse();
    }

    @Test
    void testJwtService_GenerateToken_StandardUser() {
        UserDetails standardUser = org.springframework.security.core.userdetails.User.builder()
                .username("standard")
                .password("pass")
                .roles("VECINO")
                .build();
        String token = jwtService.generateToken(standardUser);
        assertThat(token).isNotEmpty();
    }

    @Test
    void testFilter_NullUserRut() throws Exception {
        UserDetails userWithNullRut = mock(UserDetails.class);
        when(userWithNullRut.getUsername()).thenReturn(null);
        String token = jwtService.generateToken(userWithNullRut);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
