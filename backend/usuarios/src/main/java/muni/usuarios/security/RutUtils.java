package muni.usuarios.security;

public class RutUtils {

    public static String formatRut(String rut) {
        if (rut == null) {
            return null;
        }
        // Clean the RUT: remove dots, hyphens, spaces
        String cleaned = rut.replace(".", "").replace("-", "").trim();
        if (cleaned.length() < 2) {
            return rut;
        }
        
        String body = cleaned.substring(0, cleaned.length() - 1);
        String dv = cleaned.substring(cleaned.length() - 1).toUpperCase();
        
        // Format body with dots
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = body.length() - 1; i >= 0; i--) {
            sb.insert(0, body.charAt(i));
            count++;
            if (count % 3 == 0 && i > 0) {
                sb.insert(0, ".");
            }
        }
        
        return sb.toString() + "-" + dv;
    }
}
