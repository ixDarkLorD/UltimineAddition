package net.ixdarklord.ultimine_addition.util;

import net.minecraft.ChatFormatting;

import java.util.Map;

public class ChatFormattingUtils {
    public static ChatFormatting getAssignedResult(int currentValue, int maxValue, Map<Integer, ChatFormatting[]> percentageMapping) {
        // Calculate completion percentage
        double completionPercentage = ((double) currentValue / maxValue) * 100;

        // Determine the assigned value based on the custom mapping
        for (Map.Entry<Integer, ChatFormatting[]> entry : percentageMapping.entrySet()) {
            if (completionPercentage >= entry.getKey()) {
                return entry.getValue()[0];
            }
        }

        // Default value for incomplete
        return ChatFormatting.WHITE;
    }
}
