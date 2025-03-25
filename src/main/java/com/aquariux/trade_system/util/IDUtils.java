package com.aquariux.trade_system.util;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@UtilityClass
public class IDUtils {
    public static String generateShortUUID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }
}
