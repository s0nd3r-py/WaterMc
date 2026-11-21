package me.s0nd3r.watermc.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/** Встроенные функции и члены текста/списков. */
final class Builtins {

    @FunctionalInterface
    interface Fn {
        Object apply(Exec.Execution ex, List<Object> args);
    }

    record Builtin(String name, int minArgs, int maxArgs, String signature, Fn fn) {
    }

    static final Map<String, Builtin> FUNCTIONS = new TreeMap<>();

    private static void def(final String signature, final int min, final int max, final Fn fn) {
        final String name = signature.substring(0, signature.indexOf('('));
        FUNCTIONS.put(name, new Builtin(name, min, max, signature, fn));
    }

    private static double num(final List<Object> args, final int i, final String fn) {
        return Values.number(args.get(i), fn + ": аргумент " + (i + 1));
    }

    static {
        // числа
        def("random(min, max)", 0, 2, (ex, a) -> {
            final ThreadLocalRandom r = ThreadLocalRandom.current();
            if (a.isEmpty()) {
                return r.nextDouble();
            }
            final long min = a.size() == 1 ? 0 : (long) Math.floor(num(a, 0, "random"));
            final long max = (long) Math.floor(num(a, a.size() - 1, "random"));
            if (max < min) {
                throw new IllegalArgumentException("random: max меньше min");
            }
            return (double) r.nextLong(min, max + 1);
        });
        def("chance(percent)", 1, 1, (ex, a) -> ThreadLocalRandom.current().nextDouble() * 100.0 < num(a, 0, "chance"));
        def("round(x, digits)", 1, 2, (ex, a) -> {
            final double x = num(a, 0, "round");
            if (a.size() == 1) {
                return (double) Math.round(x);
            }
            final double scale = Math.pow(10, Math.max(0, Math.min(10, (int) num(a, 1, "round"))));
            return Math.round(x * scale) / scale;
        });
        def("floor(x)", 1, 1, (ex, a) -> Math.floor(num(a, 0, "floor")));
        def("ceil(x)", 1, 1, (ex, a) -> Math.ceil(num(a, 0, "ceil")));
        def("abs(x)", 1, 1, (ex, a) -> Math.abs(num(a, 0, "abs")));
        def("sqrt(x)", 1, 1, (ex, a) -> Math.sqrt(num(a, 0, "sqrt")));
        def("pow(x, y)", 2, 2, (ex, a) -> Math.pow(num(a, 0, "pow"), num(a, 1, "pow")));
        def("min(a, b, ...)", 1, 64, (ex, a) -> extreme(a, true));
        def("max(a, b, ...)", 1, 64, (ex, a) -> extreme(a, false));
        def("clamp(x, min, max)", 3, 3, (ex, a) -> Math.max(num(a, 1, "clamp"), Math.min(num(a, 2, "clamp"), num(a, 0, "clamp"))));
        def("now()", 0, 0, (ex, a) -> (double) System.currentTimeMillis());

        // типы и текст
        def("str(x)", 1, 1, (ex, a) -> Values.display(a.get(0)));
        def("num(x)", 1, 1, (ex, a) -> {
            final Object v = a.get(0);
            if (v instanceof Double) {
                return v;
            }
            try {
                return Double.parseDouble(Values.display(v).trim().replace(',', '.'));
            } catch (final NumberFormatException ex2) {
                return null;
            }
        });
        def("type(x)", 1, 1, (ex, a) -> Values.typeOf(a.get(0)));
        def("len(x)", 1, 1, (ex, a) -> {
            final Object v = a.get(0);
            if (v instanceof String s) {
                return (double) s.length();
            }
            if (v instanceof List<?> l) {
                return (double) l.size();
            }
            throw new IllegalArgumentException("len: ожидался текст или список, а не " + Values.typeOf(v));
        });
        def("lower(text)", 1, 1, (ex, a) -> Values.text(a.get(0), "lower").toLowerCase(Locale.ROOT));
        def("upper(text)", 1, 1, (ex, a) -> Values.text(a.get(0), "upper").toUpperCase(Locale.ROOT));

        // списки
        def("list(items...)", 0, 64, (ex, a) -> new ArrayList<>(a));
        def("range(from, to)", 1, 2, (ex, a) -> {
            final long from = a.size() == 1 ? 0 : (long) num(a, 0, "range");
            final long to = (long) num(a, a.size() - 1, "range");
            Values.checkListSize((int) Math.max(0, Math.min(Integer.MAX_VALUE, to - from)));
            final List<Object> out = new ArrayList<>((int) Math.max(0, to - from));
            for (long i = from; i < to; i++) {
                out.add((double) i);
            }
            return out;
        });
        def("add(list, item)", 2, 2, (ex, a) -> {
            final List<Object> list = Values.list(a.get(0), "add");
            Values.checkListSize(list.size() + 1);
            list.add(a.get(1));
            return list;
        });
        def("remove(list, item)", 2, 2, (ex, a) -> {
            final List<Object> list = Values.list(a.get(0), "remove");
            for (int i = 0; i < list.size(); i++) {
                if (Values.equal(list.get(i), a.get(1))) {
                    list.remove(i);
                    return true;
                }
            }
            return false;
        });
        def("join(list, separator)", 1, 2, (ex, a) -> {
            final List<Object> list = Values.list(a.get(0), "join");
            final String sep = a.size() > 1 ? Values.display(a.get(1)) : ", ";
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(sep);
                }
                sb.append(Values.display(list.get(i)));
            }
            return Values.checkedString(sb.toString());
        });
        def("split(text, separator)", 2, 2, (ex, a) -> {
            final String text = Values.text(a.get(0), "split");
            final String sep = Values.text(a.get(1), "split");
            final List<Object> out = new ArrayList<>();
            if (sep.isEmpty()) {
                for (int i = 0; i < text.length(); i++) {
                    out.add(String.valueOf(text.charAt(i)));
                }
            } else {
                int from = 0;
                int idx;
                while ((idx = text.indexOf(sep, from)) >= 0) {
                    out.add(text.substring(from, idx));
                    from = idx + sep.length();
                }
                out.add(text.substring(from));
            }
            Values.checkListSize(out.size());
            return out;
        });
        def("replace(text, what, with)", 3, 3, (ex, a) -> Values.checkedString(
            Values.text(a.get(0), "replace").replace(Values.display(a.get(1)), Values.display(a.get(2)))));
        def("contains(container, item)", 2, 2, (ex, a) -> Values.contains(a.get(0), a.get(1)));

        // сервер
        def("online_players()", 0, 0, (ex, a) -> new ArrayList<>(ex.runtime.host().onlinePlayers()));
        def("player(name)", 1, 1, (ex, a) -> ex.runtime.host().playerByName(Values.text(a.get(0), "player")));
        def("world(name)", 1, 1, (ex, a) -> ex.runtime.host().worldByName(Values.text(a.get(0), "world")));
    }

    private static Object extreme(final List<Object> args, final boolean min) {
        final List<?> values = args.size() == 1 && args.get(0) instanceof List<?> l ? l : args;
        if (values.isEmpty()) {
            throw new IllegalArgumentException((min ? "min" : "max") + ": пустой список");
        }
        double best = Values.number(values.get(0), min ? "min" : "max");
        for (int i = 1; i < values.size(); i++) {
            final double v = Values.number(values.get(i), min ? "min" : "max");
            best = min ? Math.min(best, v) : Math.max(best, v);
        }
        return best;
    }

    /** Свойства текста и списков: {@code text.length}, {@code list.size}, {@code list.first}… */
    static Object property(final Object target, final String name) {
        if (target instanceof String s) {
            return switch (name) {
                case "length" -> (double) s.length();
                case "upper" -> s.toUpperCase(Locale.ROOT);
                case "lower" -> s.toLowerCase(Locale.ROOT);
                case "trim" -> s.trim();
                default -> throw new IllegalArgumentException("у текста нет свойства '" + name + "'");
            };
        }
        if (target instanceof List<?> list) {
            return switch (name) {
                case "size", "length" -> (double) list.size();
                case "first" -> list.isEmpty() ? null : list.get(0);
                case "last" -> list.isEmpty() ? null : list.get(list.size() - 1);
                case "empty" -> list.isEmpty();
                default -> throw new IllegalArgumentException("у списка нет свойства '" + name + "'");
            };
        }
        if (target instanceof ScriptObject object) {
            final Object value = object.get(name);
            if (value == ScriptObject.MISSING) {
                throw new IllegalArgumentException("у " + object.typeName() + " нет свойства '" + name + "'");
            }
            return Values.normalize(value);
        }
        if (target == null) {
            throw new IllegalArgumentException("нельзя взять свойство '" + name + "' у none");
        }
        throw new IllegalArgumentException("у " + Values.typeOf(target) + " нет свойства '" + name + "'");
    }

    /** Методы текста и списков: {@code text.starts_with("a")}, {@code list.contains(x)}… */
    static Object method(final Object target, final String name, final List<Object> args) {
        if (target instanceof String s) {
            return switch (name) {
                case "contains" -> s.contains(Values.display(arg(args, 0, name)));
                case "starts_with" -> s.startsWith(Values.display(arg(args, 0, name)));
                case "ends_with" -> s.endsWith(Values.display(arg(args, 0, name)));
                case "replace" -> Values.checkedString(s.replace(Values.display(arg(args, 0, name)), Values.display(arg(args, 1, name))));
                case "split" -> FUNCTIONS.get("split").fn().apply(null, java.util.Arrays.asList(s, arg(args, 0, name)));
                default -> throw new IllegalArgumentException("у текста нет метода '" + name + "'");
            };
        }
        if (target instanceof List<?>) {
            return switch (name) {
                case "add" -> FUNCTIONS.get("add").fn().apply(null, java.util.Arrays.asList(target, arg(args, 0, name)));
                case "remove" -> FUNCTIONS.get("remove").fn().apply(null, java.util.Arrays.asList(target, arg(args, 0, name)));
                case "contains" -> Values.contains(target, arg(args, 0, name));
                case "join" -> FUNCTIONS.get("join").fn().apply(null, args.isEmpty() ? java.util.Arrays.asList(target) : java.util.Arrays.asList(target, args.get(0)));
                case "clear" -> {
                    Values.list(target, "clear").clear();
                    yield null;
                }
                default -> throw new IllegalArgumentException("у списка нет метода '" + name + "'");
            };
        }
        if (target instanceof ScriptObject object) {
            try {
                return Values.normalize(object.call(name, args));
            } catch (final UnsupportedOperationException ex) {
                throw new IllegalArgumentException("у " + object.typeName() + " нет метода '" + name + "'");
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("нельзя вызвать '" + name + "' у none");
        }
        throw new IllegalArgumentException("у " + Values.typeOf(target) + " нет метода '" + name + "'");
    }

    private static Object arg(final List<Object> args, final int i, final String method) {
        if (i >= args.size()) {
            throw new IllegalArgumentException(method + ": не хватает аргументов");
        }
        return args.get(i);
    }
}
