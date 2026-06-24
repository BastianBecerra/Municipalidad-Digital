package muni.blockchain.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentResponseTest {

    @Test
    void testLombokBoilerplateOnDocumentResponse() throws Exception {
        testDtoLombok(DocumentResponse.class);
    }

    @Test
    void testSpecificBuilder() {
        DocumentResponse r1 = DocumentResponse.builder()
                .documentId("123")
                .hash("hash")
                .transactionHash("tx")
                .timestamp("time")
                .registeredBy("reg")
                .verified(true)
                .build();

        assertThat(r1.getDocumentId()).isEqualTo("123");
        assertThat(r1.getHash()).isEqualTo("hash");
        assertThat(r1.getTransactionHash()).isEqualTo("tx");
        assertThat(r1.getTimestamp()).isEqualTo("time");
        assertThat(r1.getRegisteredBy()).isEqualTo("reg");
        assertThat(r1.isVerified()).isTrue();
        assertThat(DocumentResponse.builder().toString()).isNotEmpty();
    }

    private <T> void testDtoLombok(Class<T> clazz) throws Exception {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        T instance1 = constructor.newInstance();
        T instance2 = constructor.newInstance();

        assertThat(instance1).isEqualTo(instance2);
        assertThat(instance1.hashCode()).isEqualTo(instance2.hashCode());
        assertThat(instance1.toString()).isNotNull();

        assertThat(instance1.equals(null)).isFalse();
        assertThat(instance1.equals("not a dto")).isFalse();
        assertThat(instance1.equals(instance1)).isTrue();
        
        Method canEqualMethod = clazz.getDeclaredMethod("canEqual", Object.class);
        canEqualMethod.setAccessible(true);
        assertThat((Boolean) canEqualMethod.invoke(instance1, instance2)).isTrue();
        assertThat((Boolean) canEqualMethod.invoke(instance1, "not a dto")).isFalse();

        // Test canEqual returning false inside equals() using a Mockito Spy stubbed via Reflection
        T spyInstance = org.mockito.Mockito.spy(instance1);
        Object whenObject = org.mockito.Mockito.doReturn(false).when(spyInstance);
        canEqualMethod.invoke(whenObject, (Object) org.mockito.ArgumentMatchers.any());
        assertThat(instance1.equals(spyInstance)).isFalse();

        testFields(clazz, instance1, instance2);
    }

    private <T> void testFields(Class<T> clazz, T instance1, T instance2) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            
            String fieldName = field.getName();
            String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            
            String setterName = "set" + capitalized;
            String getterName = (field.getType() == boolean.class ? "is" : "get") + capitalized;

            Method setter = clazz.getDeclaredMethod(setterName, field.getType());
            Method getter = clazz.getDeclaredMethod(getterName);

            Object testValue = getTestValueForType(field.getType());
            if (testValue != null) {
                setter.invoke(instance1, testValue);
                Object returnedValue = getter.invoke(instance1);
                assertThat(returnedValue).isEqualTo(testValue);
                assertThat(instance1.equals(instance2)).isFalse();

                setter.invoke(instance2, testValue);
                assertThat(instance1.equals(instance2)).isTrue();
                assertThat(instance1.hashCode()).isEqualTo(instance2.hashCode());

                if (!field.getType().isPrimitive()) {
                    setter.invoke(instance1, (Object) null);
                    assertThat(instance1.equals(instance2)).isFalse();
                }

                Object alternativeValue = getAlternativeValueForType(field.getType());
                if (alternativeValue != null) {
                    setter.invoke(instance1, alternativeValue);
                    assertThat(instance1.equals(instance2)).isFalse();
                }

                setter.invoke(instance1, testValue);
                setter.invoke(instance2, testValue);
                assertThat(instance1.equals(instance2)).isTrue();
            }
        }
    }

    private Object getTestValueForType(Class<?> type) {
        if (type == String.class) return "testVal";
        if (type == boolean.class || type == Boolean.class) return true;
        return null;
    }

    private Object getAlternativeValueForType(Class<?> type) {
        if (type == String.class) return "altVal";
        if (type == boolean.class || type == Boolean.class) return false;
        return null;
    }
}
