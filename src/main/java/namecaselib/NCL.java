package namecaselib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс, который содержит основные константы библиотеки:
 * - индексы мужского и женского пола
 * - индексы всех падежей
 */
public class NCL {

    public enum Gender {
        MAN, WOMAN
    }

    /*
     * - <b>N</b> - ім’я
     * - <b>S</b> - прізвище
     * - <b>F</b> - по-батькові
     */
    enum NamePart {
        N, F, S
    }

    /**
     * Именительный падеж
     * 
     */
    public static final int IMENITLN = 0;
    
    /**
     * Родительный падеж
     * 
     */
    public static final int RODITLN = 1;
    
    /**
     * Дательный падеж
     * 
     */
    public static final int DATELN = 2;
    
    /**
     * Винительный падеж
     * 
     */
    public static final int VINITELN = 3;
    
    /**
     * Творительный падеж
     * 
     */
    public static final int TVORITELN = 4;
    
    /**
     * Предложный падеж
     * 
     */
    public static final int PREDLOGN = 5;
    
    /**
     * Назвиний відмінок
     * 
     */
    public static final int UaNazyvnyi = 0;
    
    /**
     * Родовий відмінок
     * 
     */
    public static final int UaRodovyi = 1;
    
    /**
     * Давальний відмінок
     * 
     */
    public static final int UaDavalnyi = 2;
    
    /**
     * Знахідний відмінок
     * 
     */
    public static final int UaZnahidnyi = 3;
    
    /**
     * Орудний відмінок
     * 
     */
    public static final int UaOrudnyi = 4;
    
    /**
     * Місцевий відмінок
     * 
     */
    public static final int UaMiszevyi = 5;
    
    /**
     * Кличний відмінок
     * 
     */
    public static final int UaKlychnyi = 6;

    public static String substring(String str, int start, int length) {
        if (start >= 0) {
            return str.substring(start, start + length);
        } else {
            start = str.length() + start;
            return str.substring(start, start + length);
        }
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static <T> List<T> array(T... t) {
        return new ArrayList<>(Arrays.asList(t));
    }

    public static <T> List<T> array_fill(int count, T value) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(value);
        }
        return list;
    }

}