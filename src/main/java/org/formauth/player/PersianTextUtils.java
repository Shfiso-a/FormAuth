package org.formauth.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for handling Persian text formatting
 * Based on PersianChat script
 */
public class PersianTextUtils {
    
    /**
     * Formats a text string for farsi display
     * 
     * @param text The text to format
     * @return The formatted Persian text
     */
    public static String formatPersianText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            char[] chars = text.toCharArray();
            List<String> arr = new ArrayList<>();
            if (chars.length > 0 && !isPersianChar(String.valueOf(chars[0]))) {
                return text; 
            }

            for (int i = chars.length; i > 0; i--) {
                arr.add(String.valueOf(chars[i - 1]));
            }

            for (int num = 0; num < arr.size(); num++) {
                if (num > 0 && !arr.get(num - 1).equals(" ")) {
                    arr.set(num, getFormattedChar(arr.get(num)));
                    continue;
                }
                if (arr.get(num).equals("ه")) {
                    arr.set(num, "ﻪ");
                }
            }
            
            // Build the final string
            StringBuilder formattedText = new StringBuilder();
            for (String s : arr) {
                formattedText.append(s);
            }
            
            return formattedText.toString();
        } catch (Exception e) {
            return text;
        }
    }

    private static boolean isPersianChar(String character) {
        String[] persianChars = {"ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ", "پ", "ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ", "ظ", "ط", "ز", "ر", "ذ", "د", "ئ", "و"};
        return Arrays.asList(persianChars).contains(character);
    }

    private static String getFormattedChar(String character) {
        if (isPersianChar(character)) {
            switch (character) {
                case "ض": return "ﺿ";
                case "ص": return "ﺻ";
                case "ث": return "ﺛ";
                case "ق": return "ﻗ";
                case "ف": return "ﻓ";
                case "غ": return "ﻏ";
                case "ع": return "ﻋ";
                case "ه": return "ﻫ";
                case "خ": return "ﺧ";
                case "ح": return "ﺣ";
                case "ج": return "ﺟ";
                case "چ": return "ﭼ";
                case "پ": return "ﭘ";
                case "ش": return "ﺷ";
                case "ب": return "ﺑ";
                case "س": return "ﺳ";
                case "ی": return "ﯾ";
                case "ل": return "ﻟ";
                case "ا": return "ا";
                case "ت": return "ﺗ";
                case "ن": return "ﻧ";
                case "م": return "ﻣ";
                case "ک": return "ﻛ";
                case "گ": return "ﮔ";
                case "ظ": return "ظ";
                case "ط": return "ط";
                case "ز": return "ز";
                case "ئ": return "ئ";
                default: return character;
            }
        } else {
            return character;
        }
    }
} 
