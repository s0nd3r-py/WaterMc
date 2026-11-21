package me.s0nd3r.watermc.launcher;

/**
 * Точка входа {@code java -jar watermc.jar}.
 *
 * <p>Компилируется для Java 8, чтобы на старой Java вывести понятное сообщение, а не
 * {@code UnsupportedClassVersionError}. Вся работа — в {@link Launcher}.</p>
 */
public final class Main {

    static final int REQUIRED_JAVA = 21;

    private Main() {
    }

    public static void main(final String[] args) {
        final int version = javaVersion();
        if (version < REQUIRED_JAVA) {
            System.err.println("WaterMc requires Java " + REQUIRED_JAVA + " or newer; this is Java "
                + System.getProperty("java.version") + ". Download a current JDK at https://adoptium.net/");
            System.exit(1);
        }
        try {
            Launcher.launch(args);
        } catch (final Throwable t) {
            t.printStackTrace();
            System.err.println("WaterMc could not start: " + t);
            System.exit(1);
        }
    }

    static int javaVersion() {
        final String spec = System.getProperty("java.specification.version", "0");
        final String[] parts = spec.split("\\.");
        try {
            return "1".equals(parts[0]) && parts.length > 1 ? Integer.parseInt(parts[1]) : Integer.parseInt(parts[0]);
        } catch (final NumberFormatException ex) {
            return 0;
        }
    }
}
