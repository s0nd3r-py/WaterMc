package me.s0nd3r.watermc.script;

import java.util.List;

/**
 * Объект игрового мира, доступный скриптам (игрок, блок, мир…).
 *
 * <p>Это единственный способ, которым скрипт может обратиться к серверу: доступны только явно
 * перечисленные свойства и методы. Рефлексии, доступа к файлам, сети и произвольным Java-классам нет —
 * это и есть песочница.</p>
 */
public interface ScriptObject {

    /** Имя типа для сообщений об ошибках: "player", "block", "world". */
    String typeName();

    /** Маркер «такого свойства нет» (в отличие от {@code null} — значения none). */
    Object MISSING = new Object() {
        @Override
        public String toString() {
            return "<missing>";
        }
    };

    /** Значение свойства ({@code null} — none) или {@link #MISSING}, если свойства нет. */
    Object get(String property);

    /** Устанавливает свойство; {@code false}, если свойство только для чтения или не существует. */
    default boolean set(final String property, final Object value) {
        return false;
    }

    /** Вызов метода. Бросает {@link UnsupportedOperationException}, если метода нет. */
    default Object call(final String method, final List<Object> args) {
        throw new UnsupportedOperationException(method);
    }

    /** Отображение в строке ("{player}" → имя игрока). */
    String display();
}
