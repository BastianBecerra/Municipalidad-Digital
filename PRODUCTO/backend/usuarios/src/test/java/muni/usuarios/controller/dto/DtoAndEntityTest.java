package muni.usuarios.controller.dto;

import muni.usuarios.entities.Territorio;
import muni.usuarios.entities.Usuarios;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoAndEntityTest {

    @Test
    void testLombokBoilerplateOnAllDtos() throws Exception {
        List<Class<?>> dtoClasses = List.of(
                AuthResponse.class,
                LoginRequest.class,
                RegisterRequest.class
        );

        for (Class<?> clazz : dtoClasses) {
            testDtoLombok(clazz);
        }
    }

    private <T> void testDtoLombok(Class<T> clazz) throws Exception {
        // Test No-ArgsConstructor
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        T instance1 = constructor.newInstance();
        T instance2 = constructor.newInstance();

        // Verify equals on empty instances
        assertThat(instance1).isEqualTo(instance2);
        assertThat(instance1.hashCode()).isEqualTo(instance2.hashCode());
        assertThat(instance1.toString()).isNotNull();

        // Basic equals checks for coverage of equals(null), equals(wrong class), equals(self)
        assertThat(instance1.equals(null)).isFalse();
        assertThat(instance1.equals("not a dto")).isFalse();
        assertThat(instance1.equals(instance1)).isTrue();
        
        // Test canEqual method
        Method canEqualMethod = null;
        try {
            canEqualMethod = clazz.getMethod("canEqual", Object.class);
            assertThat((Boolean) canEqualMethod.invoke(instance1, instance2)).isTrue();
            assertThat((Boolean) canEqualMethod.invoke(instance1, "not a dto")).isFalse();
        } catch (NoSuchMethodException e) {
            try {
                canEqualMethod = clazz.getDeclaredMethod("canEqual", Object.class);
                canEqualMethod.setAccessible(true);
                assertThat((Boolean) canEqualMethod.invoke(instance1, instance2)).isTrue();
                assertThat((Boolean) canEqualMethod.invoke(instance1, "not a dto")).isFalse();
            } catch (NoSuchMethodException ex) {}
        }

        // Test canEqual returning false inside equals() using a Mockito Spy stubbed via Reflection
        if (canEqualMethod != null) {
            T spyInstance = org.mockito.Mockito.spy(instance1);
            Object whenObject = org.mockito.Mockito.doReturn(false).when(spyInstance);
            canEqualMethod.invoke(whenObject, (Object) org.mockito.ArgumentMatchers.any());
            assertThat(instance1.equals(spyInstance)).isFalse();
        }

        // Get all declared fields and test getters, setters, and comparison branches
        testFields(clazz, instance1, instance2);
    }

    private <T> void testFields(Class<T> clazz, T instance1, T instance2) throws Exception {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                
                String fieldName = field.getName();
                String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                
                // Getter and Setter name
                String setterName = "set" + capitalized;
                String getterName = (field.getType() == boolean.class ? "is" : "get") + capitalized;

                Method setter = null;
                Method getter = null;

                try {
                    setter = current.getDeclaredMethod(setterName, field.getType());
                } catch (NoSuchMethodException e) {}

                try {
                    getter = current.getDeclaredMethod(getterName);
                } catch (NoSuchMethodException e) {}

                if (setter != null && getter != null) {
                    Object testValue = getTestValueForType(field.getType());
                    if (testValue != null) {
                        // 1. Set field on instance1 to testValue, instance2 is null.
                        // (tests this != null, other == null)
                        setter.invoke(instance1, testValue);
                        
                        // Verify getter works
                        Object returnedValue = getter.invoke(instance1);
                        assertThat(returnedValue).isEqualTo(testValue);
                        
                        assertThat(instance1.equals(instance2)).isFalse();

                        // 2. Set field on instance2 to testValue, instance1 is testValue.
                        // (tests this != null, other != null, equal)
                        setter.invoke(instance2, testValue);
                        assertThat(instance1.equals(instance2)).isTrue();
                        assertThat(instance1.hashCode()).isEqualTo(instance2.hashCode());

                        // 3. Set field on instance1 to null, instance2 is testValue.
                        // (tests this == null, other != null)
                        setter.invoke(instance1, (Object) null);
                        assertThat(instance1.equals(instance2)).isFalse();

                        // 4. Test equals with different values
                        Object alternativeValue = getAlternativeValueForType(field.getType());
                        if (alternativeValue != null) {
                            // Set instance1 to alternativeValue, instance2 is testValue
                            setter.invoke(instance1, alternativeValue);
                            assertThat(instance1.equals(instance2)).isFalse();
                        }

                        // Restore both back to testValue so they are equal for the next field
                        setter.invoke(instance1, testValue);
                        setter.invoke(instance2, testValue);
                        assertThat(instance1.equals(instance2)).isTrue();
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    private Object getTestValueForType(Class<?> type) {
        if (type == String.class) return "testVal";
        if (type == Long.class || type == long.class) return 42L;
        if (type == Integer.class || type == int.class) return 42;
        if (type == Double.class || type == double.class) return 42.42;
        if (type == boolean.class || type == Boolean.class) return true;
        if (type == LocalDate.class) return LocalDate.of(2000, 1, 1);
        return null;
    }

    private Object getAlternativeValueForType(Class<?> type) {
        if (type == String.class) return "altVal";
        if (type == Long.class || type == long.class) return 99L;
        if (type == Integer.class || type == int.class) return 99;
        if (type == Double.class || type == double.class) return 99.99;
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == LocalDate.class) return LocalDate.of(2020, 1, 1);
        return null;
    }

    @Test
    void testAuthResponseSpecific() {
        AuthResponse r1 = AuthResponse.builder().token("tok").build();
        AuthResponse r2 = new AuthResponse("tok");
        assertThat(r1.getToken()).isEqualTo("tok");
        assertThat(r2.getToken()).isEqualTo("tok");
        assertThat(AuthResponse.builder().toString()).isNotEmpty();
    }

    @Test
    void testLoginRequestSpecific() {
        LoginRequest r1 = LoginRequest.builder()
                .rut("123")
                .email("a@b.com")
                .password("pass")
                .build();
        LoginRequest r2 = new LoginRequest("123", "a@b.com", "pass");
        assertThat(r1.getRut()).isEqualTo("123");
        assertThat(LoginRequest.builder().toString()).isNotEmpty();
    }

    @Test
    void testRegisterRequestSpecific() {
        RegisterRequest r1 = RegisterRequest.builder()
                .nombres("Nom")
                .apellidoPaterno("Pat")
                .apellidoMaterno("Mat")
                .rut("123")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .genero("M")
                .email("u@e.com")
                .telefono("123")
                .direccion("Dir")
                .comuna("Com")
                .region("Reg")
                .password("p")
                .passwordClaveUnica("cu")
                .rol("VECINO")
                .territorioId(1L)
                .build();

        RegisterRequest r2 = new RegisterRequest(
                "Nom", "Pat", "Mat", "123", LocalDate.of(2000, 1, 1), "M", "u@e.com", "123", "Dir", "Com", "Reg", "p", "cu", "VECINO", 1L
        );
        assertThat(r1.getNombres()).isEqualTo("Nom");
        assertThat(RegisterRequest.builder().toString()).isNotEmpty();
    }

    @Test
    void testTerritorio() throws Exception {
        Territorio t1 = Territorio.builder()
                .id(1L)
                .nombre("T1")
                .tipo("Tipo")
                .numeroUnidadVecinal("10")
                .comuna("Com")
                .region("Reg")
                .direccionSede("Sede")
                .latitud(1.0)
                .longitud(2.0)
                .limiteNorte("N")
                .limiteSur("S")
                .limiteEste("E")
                .limiteOeste("O")
                .email("e@a.com")
                .telefono("123")
                .presidente("Pres")
                .descripcion("Desc")
                .activo(true)
                .vecinos(Collections.emptyList())
                .fechaCreacion(LocalDateTime.now())
                .ultimaActualizacion(LocalDateTime.now())
                .build();

        Territorio t2 = new Territorio();
        t2.setId(1L);
        t2.setNombre("T1");
        t2.setTipo("Tipo");
        t2.setNumeroUnidadVecinal("10");
        t2.setComuna("Com");
        t2.setRegion("Reg");
        t2.setDireccionSede("Sede");
        t2.setLatitud(1.0);
        t2.setLongitud(2.0);
        t2.setLimiteNorte("N");
        t2.setLimiteSur("S");
        t2.setLimiteEste("E");
        t2.setLimiteOeste("O");
        t2.setEmail("e@a.com");
        t2.setTelefono("123");
        t2.setPresidente("Pres");
        t2.setDescripcion("Desc");
        t2.setActivo(true);
        t2.setVecinos(Collections.emptyList());
        t2.setFechaCreacion(t1.getFechaCreacion());
        t2.setUltimaActualizacion(t1.getUltimaActualizacion());

        assertThat(t1.getNombre()).isEqualTo("T1");
        assertThat(t1.getTipo()).isEqualTo("Tipo");
        assertThat(t1.getNumeroUnidadVecinal()).isEqualTo("10");
        assertThat(t1.getComuna()).isEqualTo("Com");
        assertThat(t1.getRegion()).isEqualTo("Reg");
        assertThat(t1.getDireccionSede()).isEqualTo("Sede");
        assertThat(t1.getLatitud()).isEqualTo(1.0);
        assertThat(t1.getLongitud()).isEqualTo(2.0);
        assertThat(t1.getLimiteNorte()).isEqualTo("N");
        assertThat(t1.getLimiteSur()).isEqualTo("S");
        assertThat(t1.getLimiteEste()).isEqualTo("E");
        assertThat(t1.getLimiteOeste()).isEqualTo("O");
        assertThat(t1.getEmail()).isEqualTo("e@a.com");
        assertThat(t1.getTelefono()).isEqualTo("123");
        assertThat(t1.getPresidente()).isEqualTo("Pres");
        assertThat(t1.getDescripcion()).isEqualTo("Desc");
        assertThat(t1.getActivo()).isTrue();
        assertThat(t1.getVecinos()).isEmpty();
        assertThat(t1.getFechaCreacion()).isNotNull();
        assertThat(t1.getUltimaActualizacion()).isNotNull();

        assertThat(t1.toString()).isNotEmpty();
        assertThat(Territorio.builder().toString()).isNotEmpty();

        // Probar onCreate y onUpdate via reflexion
        Method onCreateMethod = Territorio.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);

        Territorio t3 = new Territorio();
        onCreateMethod.invoke(t3);
        assertThat(t3.getActivo()).isTrue();
        assertThat(t3.getFechaCreacion()).isNotNull();
        assertThat(t3.getUltimaActualizacion()).isNotNull();

        Territorio t4 = new Territorio();
        t4.setActivo(false);
        onCreateMethod.invoke(t4);
        assertThat(t4.getActivo()).isFalse();

        Method onUpdateMethod = Territorio.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        onUpdateMethod.invoke(t3);
        assertThat(t3.getUltimaActualizacion()).isNotNull();
    }

    @Test
    void testUsuarios() throws Exception {
        Usuarios u1 = Usuarios.builder()
                .id(1L)
                .nombres("U1")
                .apellidoPaterno("Pat")
                .apellidoMaterno("Mat")
                .rut("123")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .genero("M")
                .email("u@e.com")
                .telefono("123")
                .direccion("Dir")
                .comuna("Com")
                .region("Reg")
                .password("p")
                .passwordClaveUnica("cu")
                .rol("VECINO")
                .activo(true)
                .territorio(new Territorio())
                .fechaRegistro(LocalDateTime.now())
                .ultimaActualizacion(LocalDateTime.now())
                .build();

        Usuarios u2 = new Usuarios();
        u2.setId(1L);
        u2.setNombres("U1");
        u2.setApellidoPaterno("Pat");
        u2.setApellidoMaterno("Mat");
        u2.setRut("123");
        u2.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        u2.setGenero("M");
        u2.setEmail("u@e.com");
        u2.setTelefono("123");
        u2.setDireccion("Dir");
        u2.setComuna("Com");
        u2.setRegion("Reg");
        u2.setPassword("p");
        u2.setPasswordClaveUnica("cu");
        u2.setRol("VECINO");
        u2.setActivo(true);
        u2.setTerritorio(u1.getTerritorio());
        u2.setFechaRegistro(u1.getFechaRegistro());
        u2.setUltimaActualizacion(u1.getUltimaActualizacion());

        assertThat(u1.getNombres()).isEqualTo("U1");
        assertThat(u1.getApellidoPaterno()).isEqualTo("Pat");
        assertThat(u1.getApellidoMaterno()).isEqualTo("Mat");
        assertThat(u1.getRut()).isEqualTo("123");
        assertThat(u1.getFechaNacimiento()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(u1.getGenero()).isEqualTo("M");
        assertThat(u1.getEmail()).isEqualTo("u@e.com");
        assertThat(u1.getTelefono()).isEqualTo("123");
        assertThat(u1.getDireccion()).isEqualTo("Dir");
        assertThat(u1.getComuna()).isEqualTo("Com");
        assertThat(u1.getRegion()).isEqualTo("Reg");
        assertThat(u1.getPassword()).isEqualTo("p");
        assertThat(u1.getPasswordClaveUnica()).isEqualTo("cu");
        assertThat(u1.getRol()).isEqualTo("VECINO");
        assertThat(u1.getActivo()).isTrue();
        assertThat(u1.getTerritorio()).isNotNull();
        assertThat(u1.getFechaRegistro()).isNotNull();
        assertThat(u1.getUltimaActualizacion()).isNotNull();

        assertThat(u1.toString()).isNotEmpty();
        assertThat(Usuarios.builder().toString()).isNotEmpty();

        assertThat(u1.getUsername()).isEqualTo("123");
        assertThat(u1.getAuthorities()).hasSize(1);
        assertThat(u1.isAccountNonExpired()).isTrue();
        assertThat(u1.isAccountNonLocked()).isTrue();
        assertThat(u1.isCredentialsNonExpired()).isTrue();
        assertThat(u1.isEnabled()).isTrue();

        // Probar builder default de activo
        Usuarios uDefault = Usuarios.builder().build();
        assertThat(uDefault.getActivo()).isTrue();

        // Probar onCreate y onUpdate via reflexion
        Method onCreateMethod = Usuarios.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);

        Usuarios u3 = new Usuarios();
        u3.setActivo(null); // force it to null to test the null branch
        onCreateMethod.invoke(u3);
        assertThat(u3.getActivo()).isTrue();

        Usuarios u4 = new Usuarios();
        u4.setActivo(false);
        onCreateMethod.invoke(u4);
        assertThat(u4.getActivo()).isFalse();

        Method onUpdateMethod = Usuarios.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        onUpdateMethod.invoke(u3);
        assertThat(u3.getUltimaActualizacion()).isNotNull();
    }
}
