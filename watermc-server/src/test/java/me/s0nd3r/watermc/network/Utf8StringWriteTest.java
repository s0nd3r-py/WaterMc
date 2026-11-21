package me.s0nd3r.watermc.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import java.util.Random;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Direct UTF-8 write produces exactly the bytes of the old temporary-buffer version, lone surrogates included. */
class Utf8StringWriteTest {

    private static byte[] old(CharSequence string) {
        ByteBuf tmp = Unpooled.buffer();
        int len = ByteBufUtil.writeUtf8(tmp, string);
        ByteBuf out = Unpooled.buffer();
        VarInt.write(out, len);
        out.writeBytes(tmp);
        return ByteBufUtil.getBytes(out);
    }

    @Test
    void sameBytesAsBefore() {
        Random random = new Random(5);
        char[] pool = {'a', 'Z', '0', ' ', 'é', 'Ж', '中', '\uD83D', '\uDE00', '\u0000', '￿'};
        for (int run = 0; run < 5000; run++) {
            StringBuilder sb = new StringBuilder();
            for (int i = random.nextInt(300); i > 0; i--) {
                sb.append(pool[random.nextInt(pool.length)]);
            }
            ByteBuf buf = Unpooled.buffer();
            Utf8String.write(buf, sb, 32767);
            assertArrayEquals(old(sb), ByteBufUtil.getBytes(buf), sb.toString());
        }
    }

    @Test
    void lengthLimitStillThrows() {
        assertThrows(EncoderException.class, () -> Utf8String.write(Unpooled.buffer(), "abcd", 3));
        ByteBuf ok = Unpooled.buffer();
        Utf8String.write(ok, "中中中", 3);
        assertEquals("中中中", Utf8String.read(ok, 3));
    }
}
