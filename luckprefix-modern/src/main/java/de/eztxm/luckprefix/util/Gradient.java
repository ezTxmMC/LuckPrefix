package de.eztxm.luckprefix.util;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;

public class Gradient {
    private final String startColor;
    private final String endColor;

    public Gradient(String startColor, String endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public String getGradientString(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        Color start = Color.decode(startColor);
        Color end = Color.decode(endColor);
        int length = text.length();
        for (int i = 0; i < length; i++) {
            double ratio = (double) i / (double) (length - 1);
            int red = (int) (end.getRed() * ratio + start.getRed() * (1 - ratio));
            int green = (int) (end.getGreen() * ratio + start.getGreen() * (1 - ratio));
            int blue = (int) (end.getBlue() * ratio + start.getBlue() * (1 - ratio));
            Color color = new Color(red, green, blue);
            stringBuilder.append("§x").append(ChatColor.of(color));
            stringBuilder.append(text.charAt(i));
        }
        return stringBuilder.toString();
    }
}
