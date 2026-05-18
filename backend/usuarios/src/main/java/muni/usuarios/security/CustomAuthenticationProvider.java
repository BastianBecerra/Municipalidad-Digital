package muni.usuarios.security;

import lombok.RequiredArgsConstructor;
import muni.usuarios.entities.Usuarios;
import muni.usuarios.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName(); // Puede ser RUT o Email
        String password = authentication.getCredentials().toString();

        boolean isEmail = username.contains("@");
        Optional<Usuarios> usuarioOpt = isEmail ? usuarioRepository.findByEmail(username) : usuarioRepository.findByRut(username);

        if (usuarioOpt.isEmpty()) {
            throw new BadCredentialsException("Usuario no encontrado");
        }

        Usuarios usuario = usuarioOpt.get();
        boolean passwordMatch = false;

        if (isEmail) {
            // Login por Email -> Usa la contraseña tradicional
            if (usuario.getPassword() != null) {
                passwordMatch = passwordEncoder.matches(password, usuario.getPassword());
            }
        } else {
            // Login por RUT -> Usa la clave única
            if (usuario.getPasswordClaveUnica() != null) {
                passwordMatch = passwordEncoder.matches(password, usuario.getPasswordClaveUnica());
            }
        }

        if (!passwordMatch) {
            throw new BadCredentialsException("Contraseña incorrecta para el método de ingreso seleccionado");
        }

        if (!usuario.getActivo()) {
            throw new BadCredentialsException("El usuario está desactivado");
        }

        // Si todo está correcto, retorna el token autenticado
        return new UsernamePasswordAuthenticationToken(usuario.getUsername(), password, usuario.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
