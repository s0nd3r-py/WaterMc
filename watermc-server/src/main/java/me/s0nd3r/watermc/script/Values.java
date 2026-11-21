package me.s0nd3r.watermc.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Значения WaterScript: {@code null} (none), {@link Double} (число), {@link String} (текст),
 * {@link Boolean}, {@link List} (список) и {@link ScriptObject} (игровой объект).
 */
final class Values {

    static final int MAX_LIST_SIZE = 100_000;
    static final int MAX_STRING_LENGTH = 1_000_000;

    private Values() {
    }

    static String typeOf(final Object value) {
        if (value == null) {
            return "none";
        }
        if (value instanceof Double) {
            return "number";
        }
        if (value instanceof String) {
            return "text";
        }
        if (value instanceof Boolean) {
            return "boolean";
        }
        if (value instanceof List) {
            return "list";
        }
        if (value instanceof ScriptObject object) {
            return object.typeName();
        }
        return "unknown";
    }

    static boolean truthy(final Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Double d) {
            return d != 0.0 && !d.isNaN();
        }
        if (value instanceof String s) {
            return !s.isEmpty();
        }
        if (value instanceof List<?> list) {
            return !list.isEmpty();
        }
        return true;
    }

    static String display(final Object value) {
        if (value == null) {
            return "none";
        }
        if (value instanceof Double d) {
            return formatNumber(d);
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof List<?> list) {
            final StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(display(list.get(i)));
            }
            return sb.append(']').toString();
        }
        if (value instanceof ScriptObject object) {
            return object.display();
        }
        return String.valueOf(value);
    }

    static String formatNumber(final double d) {
        if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
            return Long.toString((long) d);
        }
        String s = String.format(Locale.ROOT, "%.6f", d);
        s = s.replaceAll("0+$", "");
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }

    /** Нормализует значения, пришедшие из Java (Integer, Long, Float…), к типам скрипта. */
    static Object normalize(final Object value) {
        if (value instanceof Number n && !(value instanceof Double)) {
            return n.doubleValue();
        }
        return value;
    }

    static double number(final Object value, final String what) {
        if (value instanceof Double d) {
            return d;
        }
        throw new IllegalArgumentException(what + " должно быть числом, а не " + typeOf(value));
    }

    static String text(final Object value, final String what) {
        if (value instanceof String s) {
            return s;
        }
        throw new IllegalArgumentException(what + " должно быть текстом, а не " + typeOf(value));
    }

    @SuppressWarnings("unchecked")
    static List<Object> list(final Object value, final String what) {
        if (value instanceof List<?> l) {
            return (List<Object>) l;
        }
        throw new IllegalArgumentException(what + " должно быть списком, а не " + typeOf(value));
    }

    static String checkedString(final String s) {
        if (s.length() > MAX_STRING_LENGTH) {
            throw new IllegalArgumentException("текст длиннее " + MAX_STRING_LENGTH + " символов");
        }
        return s;
    }

    static void checkListSize(final int size) {
        if (size > MAX_LIST_SIZE) {
            throw new IllegalArgumentException("список длиннее " + MAX_LIST_SIZE + " элементов");
        }
    }

    static boolean equal(final Object a, final Object b) {
        if (a instanceof Double x && b instanceof Double y) {
            return x.doubleValue() == y.doubleValue();
        }
        return Objects.equals(a, b);
    }

    static Object binary(final String op, final Object a, final Object b) {
        switch (op) {
            case "+" -> {
                if (a instanceof String || b instanceof String) {
                    return checkedString(display(a) + display(b));
                }
                if (a instanceof Double x && b instanceof Double y) {
                    return x + y;
                }
                if (a instanceof List<?> x && b instanceof List<?> y) {
                    checkListSize(x.size() + y.size());
                    final List<Object> out = new ArrayList<>(x);
                    out.addAll(y);
                    return out;
                }
                throw new IllegalArgumentException("нельзя сложить " + typeOf(a) + " и " + typeOf(b));
            }
            case "-" -> {
                return number(a, "левая часть '-'") - number(b, "правая часть '-'");
            }
            case "*" -> {
                if (a instanceof String s && b instanceof Double n) {
                    final double count = Math.max(0, Math.floor(n));
                    if (count * s.length() > MAX_STRING_LENGTH) {
                        throw new IllegalArgumentException("текст длиннее " + MAX_STRING_LENGTH + " символов");
                    }
                    return s.repeat((int) count);
                }
                return number(a, "левая часть '*'") * number(b, "правая часть '*'");
            }
            case "/" -> {
                final double divisor = number(b, "делитель");
                if (divisor == 0) {
                    throw new IllegalArgumentException("деление на ноль");
                }
                return number(a, "делимое") / divisor;
            }
            case "%" -> {
                final double divisor = number(b, "делитель");
                if (divisor == 0) {
                    throw new IllegalArgumentException("деление на ноль");
                }
                return number(a, "делимое") % divisor;
            }
            case "==" -> {
                return equal(a, b);
            }
            case "!=" -> {
                return !equal(a, b);
            }
            case "<", ">", "<=", ">=" -> {
                final int c = compare(a, b, op);
                return switch (op) {
                    case "<" -> c < 0;
                    case ">" -> c > 0;
                    case "<=" -> c <= 0;
                    default -> c >= 0;
                };
            }
            case "in" -> {
                return contains(b, a);
            }
            case "not in" -> {
                return !contains(b, a);
            }
            default -> throw new IllegalStateException(op);
        }
    }

    static int compare(final Object a, final Object b, final String op) {
        if (a instanceof Double x && b instanceof Double y) {
            return Double.compare(x, y);
        }
        if (a instanceof String x && b instanceof String y) {
            return x.compareTo(y);
        }
        throw new IllegalArgumentException("нельзя сравнить " + typeOf(a) + " и " + typeOf(b) + " оператором '" + op + "'");
    }

    static boolean contains(final Object container, final Object item) {
        if (container instanceof List<?> list) {
            for (final Object element : list) {
                if (equal(element, item)) {
                    return true;
                }
            }
            return false;
        }
        if (container instanceof String s) {
            return s.contains(display(item));
        }
        throw new IllegalArgumentException("'in' работает со списком или текстом, а не с " + typeOf(container));
    }

    static int index(final List<?> list, final Object index) {
        final double raw = number(index, "индекс");
        if (raw != Math.rint(raw)) {
            throw new IllegalArgumentException("индекс должен быть целым числом");
        }
        int i = (int) raw;
        if (i < 0) {
            i += list.size();
        }
        if (i < 0 || i >= list.size()) {
            throw new IllegalArgumentException("индекс " + formatNumber(raw) + " вне списка из " + list.size() + " элементов");
        }
        return i;
    }
}
