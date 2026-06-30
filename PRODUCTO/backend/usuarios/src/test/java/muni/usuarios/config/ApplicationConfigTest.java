package muni.usuarios.config;

import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void testBeans() throws Exception {
        ApplicationConfig config = new ApplicationConfig(usuarioRepository);

        // Test passwordEncoder
        assertThat(config.passwordEncoder()).isInstanceOf(BCryptPasswordEncoder.class);

        // Test authenticationProvider
        assertThat(config.authenticationProvider()).isNotNull();

        // Test authenticationManager
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(manager);
        assertThat(config.authenticationManager(authConfig)).isSameAs(manager);

        // Test userDetailsService
        UserDetailsService service = config.userDetailsService();
        assertThat(service).isNotNull();

        // Scenario 1: RUT found
        Usuarios u = new Usuarios();
        u.setRut("12-3");
        when(usuarioRepository.findByRut("12-3")).thenReturn(Optional.of(u));
        assertThat(service.loadUserByUsername("123")).isSameAs(u);

        // Scenario 2: RUT not found, Email found
        when(usuarioRepository.findByRut("45-6")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("45-6")).thenReturn(Optional.of(u));
        assertThat(service.loadUserByUsername("456")).isSameAs(u);

        // Scenario 3: Neither found
        when(usuarioRepository.findByRut("78-9")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("78-9")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("789"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con RUT o Email");
    }
}
