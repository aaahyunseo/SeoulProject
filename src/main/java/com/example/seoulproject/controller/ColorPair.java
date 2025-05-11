package com.example.seoulproject.controller;

public class ColorPair {
    private final String lightColor;
    private final String darkColor;

    public ColorPair(String lightColor, String darkColor) {
        this.lightColor = lightColor;
        this.darkColor = darkColor;
    }

    public String getLightColor() {
        return lightColor;
    }

    public String getDarkColor() {
        return darkColor;
    }

    // 정적 메서드로 추가
    public static String hslToHex(float h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = l - c / 2;

        float r = 0, g = 0, b = 0;
        if (h < 60) { r = c; g = x; }
        else if (h < 120) { r = x; g = c; }
        else if (h < 180) { g = c; b = x; }
        else if (h < 240) { g = x; b = c; }
        else if (h < 300) { r = x; b = c; }
        else { r = c; b = x; }

        int r255 = (int) ((r + m) * 255);
        int g255 = (int) ((g + m) * 255);
        int b255 = (int) ((b + m) * 255);

        return String.format("#%02X%02X%02X", r255, g255, b255);
    }

    public static ColorPair getColorPairForField(String fieldName) {
        int hue = fieldName.hashCode() % 360;
        float saturation = 0.5f;
        float lightness = 0.85f;

        String lightColor = hslToHex(hue, saturation, lightness);
        String darkColor = hslToHex(hue, saturation, 0.35f);

        return new ColorPair(lightColor, darkColor);
    }
}

