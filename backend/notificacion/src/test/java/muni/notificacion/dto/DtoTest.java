package muni.notificacion.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    @Test
    void testLombokBoilerplateOnAllDtos() throws Exception {
        List<Class<?>> dtoClasses = List.of(
                DocumentoAprobadoRequest.class,
                RestablecerPasswordRequest.class
        );

        for (Class<?> clazz : dtoClasses) {
            testDtoLombok(clazz);
        }
    }

    @Test
    void testBuilders() {
        DocumentoAprobadoRequest r1 = DocumentoAprobadoRequest.builder()
                .email("test@example.com")
                .nombreCompleto("Juan")
                .documentId("DOC-1")
                .tipoDocumento("Tipo")
                .titulo("Titulo")
                .hashBlockchain("Hash")
                .build();

        assertThat(r1.getEmail()).isEqualTo("test@example.com");
        assertThat(r1.getNombreCompleto()).isEqualTo("Juan");
        assertThat(r1.getDocumentId()).isEqualTo("DOC-1");
        assertThat(r1.getTipoDocumento()).isEqualTo("Tipo");
        assertThat(r1.getTitulo()).isEqualTo("Titulo");
        assertThat(r1.getHashBlockchain()).isEqualTo("Hash");

        // Verify Builder toString
        assertThat(DocumentoAprobadoRequest.builder().toString()).isNotNull();

        RestablecerPasswordRequest r2 = RestablecerPasswordRequest.builder()
                .email("test2@example.com")
                .nombreCompleto("María")
                .token("tok")
                .urlRestablecer("url")
                .build();

        assertThat(r2.getEmail()).isEqualTo("test2@example.com");
        assertThat(r2.getNombreCompleto()).isEqualTo("María");
        assertThat(r2.getToken()).isEqualTo("tok");
        assertThat(r2.getUrlRestablecer()).isEqualTo("url");

        // Verify Builder toString
        assertThat(RestablecerPasswordRequest.builder().toString()).isNotNull();
    }

    @Test
    void testAllArgsConstructorConstructors() {
        DocumentoAprobadoRequest r1 = new DocumentoAprobadoRequest("a", "b", "c", "d", "e", "f");
        assertThat(r1.getEmail()).isEqualTo("a");

        RestablecerPasswordRequest r2 = new RestablecerPasswordRequest("a", "b", "c", "d");
        assertThat(r2.getEmail()).isEqualTo("a");
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
        return null;
    }

    private Object getAlternativeValueForType(Class<?> type) {
        if (type == String.class) return "altVal";
        if (type == Long.class || type == long.class) return 99L;
        if (type == Integer.class || type == int.class) return 99;
        if (type == Double.class || type == double.class) return 99.99;
        if (type == boolean.class || type == Boolean.class) return false;
        return null;
    }
}
