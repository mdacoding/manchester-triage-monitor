package de.hospital.triagedashboard.security;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Hilfsklasse für die CORS-Origin-Liste.
 *
 * {@code CORS_ALLOWED_ORIGINS} kann im Hosting-Dashboard gesetzt werden, ist
 * aber leicht veraltet (z. B. nach einem Vercel-Projekt-Rename). Deshalb
 * werden bekannte Demo-/Portfolio-Origins immer zusätzlich erlaubt.
 */
final class CorsOrigins {

    static final List<String> BUILTIN_DEMO_ORIGINS = List.of(
            "http://localhost:5173",
            "https://manchester-triage-monitor.vercel.app",
            "https://frontend-six-pink-37.vercel.app"
    );

    private CorsOrigins() {
    }

    static List<String> merge(String configured) {
        Set<String> origins = new LinkedHashSet<>();
        if (configured != null) {
            for (String part : configured.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    origins.add(trimmed);
                }
            }
        }
        origins.addAll(BUILTIN_DEMO_ORIGINS);
        return List.copyOf(origins);
    }
}
