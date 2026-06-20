package net.ixdarklord.ultimine_addition.util;

import com.mojang.brigadier.StringReader;
import net.minecraft.CharPredicate;

public class ParserUtils {
    public static String readWhile(StringReader reader, CharPredicate predicate) {
        int i = reader.getCursor();

        while (reader.canRead() && predicate.test(reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(i, reader.getCursor());
    }
}
