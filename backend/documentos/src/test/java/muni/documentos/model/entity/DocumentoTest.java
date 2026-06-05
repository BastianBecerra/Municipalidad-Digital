package muni.documentos.model.entity;

import muni.documentos.model.enums.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoTest {

    @Test
    void testOnCreate() {
        Documento doc = new DocumentoResidencia();
        
        assertThat(doc.getFechaCreacion()).isNull();
        assertThat(doc.getEstado()).isNull();
        assertThat(doc.getEstadoBlockchain()).isNull();

        doc.onCreate();

        assertThat(doc.getFechaCreacion()).isNotNull();
        assertThat(doc.getFechaCreacion()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(doc.getEstado()).isEqualTo(EstadoDocumento.BORRADOR);
        assertThat(doc.getEstadoBlockchain()).isEqualTo(EstadoBlockchain.PENDIENTE);
    }

    @Test
    void testOnCreateWithExistingValues() {
        Documento doc = new DocumentoResidencia();
        doc.setEstado(EstadoDocumento.FIRMADO);
        doc.setEstadoBlockchain(EstadoBlockchain.CONFIRMADO);

        doc.onCreate();

        assertThat(doc.getFechaCreacion()).isNotNull();
        assertThat(doc.getEstado()).isEqualTo(EstadoDocumento.FIRMADO);
        assertThat(doc.getEstadoBlockchain()).isEqualTo(EstadoBlockchain.CONFIRMADO);
    }

    @Test
    void testAbstractDocumentoLombok() {
        Documento doc1 = new Documento() {};
        Documento doc2 = new Documento() {};

        // Test toString
        assertThat(doc1.toString()).isNotNull();

        // Test canEqual
        assertThat(doc1.canEqual(doc2)).isTrue();
        assertThat(doc1.canEqual("not a doc")).isFalse();

        // Test equals and hashCode
        assertThat(doc1).isEqualTo(doc2);
        assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());

        assertThat(doc1.equals(null)).isFalse();
        assertThat(doc1.equals("not a doc")).isFalse();
        assertThat(doc1.equals(doc1)).isTrue();

        // Spy to cover canEqual returning false inside equals() for the base class Documento
        Documento spyDoc = org.mockito.Mockito.spy(doc1);
        org.mockito.Mockito.doReturn(false).when(spyDoc).canEqual(org.mockito.ArgumentMatchers.any());
        assertThat(doc1.equals(spyDoc)).isFalse();

        doc1.setTitulo("title");
        assertThat(doc1).isNotEqualTo(doc2);
        doc2.setTitulo("title");
        assertThat(doc1).isEqualTo(doc2);
    }

    @Test
    void testLombokBoilerplateOnAllEntities() throws Exception {
        List<Class<? extends Documento>> entityClasses = List.of(
                DocumentoContrato.class,
                DocumentoJuntaVecinal.class,
                DocumentoLicitacion.class,
                DocumentoResidencia.class,
                DocumentoSalvoconducto.class
        );

        for (Class<? extends Documento> clazz : entityClasses) {
            testEntityLombok(clazz);
        }
    }

    private <T extends Documento> void testEntityLombok(Class<T> clazz) throws Exception {
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
        assertThat(instance1.equals("not an entity")).isFalse();
        assertThat(instance1.equals(instance1)).isTrue();
        
        // Test canEqual method
        try {
            Method canEqualMethod = clazz.getMethod("canEqual", Object.class);
            assertThat((Boolean) canEqualMethod.invoke(instance1, instance2)).isTrue();
            assertThat((Boolean) canEqualMethod.invoke(instance1, "not an entity")).isFalse();
        } catch (NoSuchMethodException e) {
            try {
                Method canEqualMethod = clazz.getDeclaredMethod("canEqual", Object.class);
                canEqualMethod.setAccessible(true);
                assertThat((Boolean) canEqualMethod.invoke(instance1, instance2)).isTrue();
                assertThat((Boolean) canEqualMethod.invoke(instance1, "not an entity")).isFalse();
            } catch (NoSuchMethodException ex) {}
        }

        // Test canEqual returning false inside equals() using a Mockito Spy
        T spyInstance = org.mockito.Mockito.spy(instance1);
        org.mockito.Mockito.doReturn(false).when(spyInstance).canEqual(org.mockito.ArgumentMatchers.any());
        assertThat(instance1.equals(spyInstance)).isFalse();

        // Get all declared fields and test getters, setters, and comparison branches
        testFields(clazz, instance1, instance2);
    }

    private <T extends Documento> void testFields(Class<T> clazz, T instance1, T instance2) throws Exception {
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
        if (type == LocalDate.class) return LocalDate.of(2026, 6, 5);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 6, 5, 12, 0);
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            if (constants != null && constants.length > 0) {
                return constants[0];
            }
        }
        return null;
    }

    private Object getAlternativeValueForType(Class<?> type) {
        if (type == String.class) return "altVal";
        if (type == Long.class || type == long.class) return 99L;
        if (type == Integer.class || type == int.class) return 99;
        if (type == Double.class || type == double.class) return 99.99;
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == LocalDate.class) return LocalDate.of(2026, 6, 6);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 6, 6, 12, 0);
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            if (constants != null && constants.length > 1) {
                return constants[1];
            }
        }
        return null;
    }
}
