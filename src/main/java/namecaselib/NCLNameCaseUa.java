package namecaselib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

import static namecaselib.NCL.NamePart.F;
import static namecaselib.NCL.NamePart.N;
import static namecaselib.NCL.NamePart.S;

/**
 * <b>NCL NameCase Ukranian Language</b>
 * 
 * Украинские правила склонений ФИО. 
 * Правила определения пола человека по ФИО для украинского языка
 * Система разделения фамилий имен и отчеств для украинского языка 
 * 

 */
public class NCLNameCaseUa extends NCLNameCaseCore {

    /**
     * Версия языкового файла
     * @var string 
     */
    protected static final String languageBuild = "11071222";

    /**
     * Количество падежей в языке
     * @var int
     */
    @Override
    public int getCaseCount() {return 7;};
    /**
     * Список гласных украинского языка
     * @var string
     */
    private static final String vowels = "аеиоуіїєюя";
    /**
     * Список согласных украинского языка
     * @var string
     */
    private static final String consonant = "бвгджзйклмнпрстфхцчшщ";
    /**
     * Українські шиплячі приголосні
     * @var string
     */
    private static final String shyplyachi = "жчшщ";
    /**
     * Українські нешиплячі приголосні
     * @var string
     */
    private static final String neshyplyachi = "бвгдзклмнпрстфхц";
    /**
     * Українські завжди м’які звуки
     * @var string
     */
    private static final String myaki = "ьюяєї";
    /**
     * Українські губні звуки
     * @var string
     */
    private static final String gubni = "мвпбф";


    @Override
    protected boolean ruleMethod(String ruleMethod) {
        switch (ruleMethod) {
            case "manRule1": return manRule1();
            case "manRule2": return manRule2();
            case "manRule3": return manRule3();
            case "manRule4": return manRule4();
            case "manRule5": return manRule5();
            case "womanRule1": return womanRule1();
            case "womanRule2": return womanRule2();
            case "womanRule3": return womanRule3();
        }
        return false;
    }

    /**
     * Чергування українських приголосних
     * Чергування г к х —» з ц с
     * @param letter літера, яку необхідно перевірити на чергування
     * @return string літера, де вже відбулося чергування
     */
    private String inverseGKH(String letter) {
        switch (letter) {
            case "г": return "з";
            case "к": return "ц";
            case "х": return "с";
        }
        return letter;
    }

    /**
     * Перевіряє чи символ є апострофом чи не є
     * @param c string(1) симпол для перевірки
     * @return bool true якщо символ є апострофом
     */
    private boolean isApostrof(String c) {
        if (this.in(c, ' ' + this.consonant + this.vowels)) {
            return false;
        }
        return true;
    }

    /**
     * Чергування українських приголосних
     * Чергування г к —» ж ч
     * @param letter літера, яку необхідно перевірити на чергування
     * @return string літера, де вже відбулося чергування
     */
    private String inverse2(String letter) {
        switch (letter) {
            case "к": return "ч";
            case "г": return "ж";
        }
        return letter;
    }

    /**
     * <b>Визначення групи для іменників 2-ї відміни</b>
     * 1 - тверда
     * 2 - мішана
     * 3 - м’яка
     *
     * <b>Правило:</b>
     * - Іменники з основою на твердий нешиплячий належать до твердої групи:
     *   береза, дорога, Дніпро, шлях, віз, село, яблуко.
     * - Іменники з основою на твердий шиплячий належать до мішаної групи:
     *   пожеж-а, пущ-а, тиш-а, алич-а, вуж, кущ, плющ, ключ, плече, прізвище.
     * - Іменники з основою на будь-який м'який чи пом'якше­ний належать до м'якої групи:
     *   земля [земл'а], зоря [зор'а], армія [арм'ійа], сім'я [с'імйа], серпень, фахівець,
     *   трамвай, су­зір'я [суз'ірйа], насіння [насін"н"а], узвишшя Іузвиш"ш"а
     * @param word іменник, групу якого необхідно визначити
     * @return int номер групи іменника
     */
    private int detect2Group(String word) {
        String osnova = word;
        LinkedList<String> stack = new LinkedList<>();
        //Ріжемо слово поки не зустрінемо приголосний і записуемо в стек всі голосні які зустріли
        while (this.in(substring(osnova, -1, 1), this.vowels + "ь")) {
            stack.add(substring(osnova, -1, 1));
            osnova = substring(osnova, 0, osnova.length() - 1);
        }
        String last = "Z"; //нульове закінчення
        if (!stack.isEmpty()) {
            last = stack.removeLast();
        }

        String osnovaEnd = substring(osnova, -1, 1);
        if (this.in(osnovaEnd, this.neshyplyachi) && !this.in(last, this.myaki)) {
            return 1;
        } else if (this.in(osnovaEnd, this.shyplyachi) && !this.in(last, this.myaki)) {
            return 2;
        }
        else {
            return 3;
        }
    }

    /**
     * Шукаємо в слові <var>word</var> перше входження літери з переліку <var>vowels</var> з кінця
     * @param word слово, якому необхідно знайти голосні
     * @param vowels перелік літер, які треба знайти
     * @return string(1) перша з кінця літера з переліку <var>vowels</var>
     */
    private String firstLastVowel(String word, String vowels) {
        int length = word.length();
        for (int i = length - 1; i > 0; i--) {
            String c = substring(word, i, 1);
            if (this.in(c, vowels)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Пошук основи іменника <var>word</var>
     * <b>Основа слова</b> - це частина слова (як правило незмінна), яка вказує на його лексичне значення.
     * @param word слово, в якому необхідно знати основу
     * @return string основа іменника <var>word</var>
     */
    private String getOsnova(String word) {
        String osnova = word;
        //Ріжемо слово поки не зустрінемо приголосний
        while (this.in(substring(osnova, -1, 1), this.vowels + "ь")) {
            osnova = substring(osnova, 0, osnova.length() - 1);
        }
        return osnova;
    }

    /**
     * Українські чоловічі та жіночі імена, що в називному відмінку однини закінчуються на -а (-я),
     * відмінються як відповідні іменники І відміни.
     * <ul>
     * <li>Примітка 1. Кінцеві приголосні основи г, к, х у жіночих іменах
     *   у давальному та місцевому відмінках однини перед закінченням -і
     *   змінюються на з, ц, с: Ольга - Ользі, Палажка - Палажці, Солоха - Солосі.</li>
     * <li>Примітка 2. У жіночих іменах типу Одарка, Параска в родовому відмінку множини
     *   в кінці основи між приголосними з'являється звук о: Одарок, Парасок. </li>
     * </ul>
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean manRule1() {
        //Предпоследний символ
        String beforeLast = this.last(2, 1);

        //Останні літера або а
        if ("a".equals(this.last(1))) {
            this.wordForms(this.workingWord, array(beforeLast + "и", this.inverseGKH(beforeLast) + "і", beforeLast + "у", beforeLast + "ою", this.inverseGKH(beforeLast) + "і", beforeLast + "о"), 2);
            this.rule(101);
            return true;
        } else if ("я".equals(this.last(1))) { //Остання літера я
            //Перед останньою літерою стоїть я
            if ("і".equals(beforeLast)) {
                this.wordForms(this.workingWord, array("ї", "ї", "ю", "єю", "ї", "є"), 1);
                this.rule(102);
                return true;
            }
            else {
                this.wordForms(this.workingWord, array(beforeLast + "і", this.inverseGKH(beforeLast) + "і", beforeLast + "ю", beforeLast + "ею", this.inverseGKH(beforeLast) + "і", beforeLast + "е"), 2);
                this.rule(103);
                return true;
            }
        }
        return false;
    }

    /**
     * Імена, що в називному відмінку закінчуються на -р, у родовому мають закінчення -а:
     * Віктор - Віктора, Макар - Макара, але: Ігор - Ігоря, Лазар - Лазаря.
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean manRule2() {
        if ("р".equals(this.last(1))) {
            if (this.inNames(this.workingWord, array("Ігор", "Лазар"))) {
                this.wordForms(this.workingWord, array("я", "еві", "я", "ем", "еві", "е"));
                this.rule(201);
                return true;
            }
            else {
                String osnova = this.workingWord;
                if ("і".equals(substring(osnova, -2, 1))) {
                    osnova = substring(osnova, 0, osnova.length() - 2) + "о" + substring(osnova, -1, 1);
                }
                this.wordForms(osnova, array("а", "ові", "а", "ом", "ові", "е"));
                this.rule(202);
                return true;
            }
        }
        return false;
    }

    /**
     * Українські чоловічі імена, що в називному відмінку однини закінчуються на приголосний та -о,
     * відмінюються як відповідні іменники ІІ відміни.
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean manRule3() {
        //Предпоследний символ
        String beforeLast = this.last(2, 1);

        if (this.in(this.last(1), this.consonant + "оь")) {
            int group = this.detect2Group(this.workingWord);
            String osnova = this.getOsnova(this.workingWord);
            //В іменах типу Антін, Нестір, Нечипір, Прокіп, Сидір, Тиміш, Федір голосний і виступає тільки в
            //називному відмінку, у непрямих - о: Антона, Антонові
            //Чергування і -» о всередині
            String osLast = substring(osnova, -1, 1);
            if (!Objects.equals(osLast, "й")
                    && "і".equals(substring(osnova, -2, 1))
                    && !this.in(substring(osnova.toLowerCase(), -4, 4), array("світ", "цвіт"))
                    && !this.inNames(this.workingWord, Arrays.asList("Гліб"))
                    && !this.in(this.last(2), array("ік", "іч"))) {
                osnova = substring(osnova, 0, osnova.length() - 2) + "о" + substring(osnova, -1, 1);
            }


            //Випадання букви е при відмінюванні слів типу Орел
            if (substring(osnova, 0, 1).equals("о") && Objects.equals(this.firstLastVowel(osnova, this.vowels + "гк"), "е") && !Objects.equals(this.last(2), "сь")) {
                int delim = osnova.lastIndexOf("е");
                osnova = substring(osnova, 0, delim) + substring(osnova, delim + 1, osnova.length() - delim);
            }


            if (group == 1) {
                //Тверда група
                //Слова що закінчуються на ок
                if (Objects.equals(this.last(2), "ок") && !Objects.equals(this.last(3), "оок")) {
                    this.wordForms(this.workingWord, array("ка", "кові", "ка", "ком", "кові", "че"), 2);
                    this.rule(301);
                    return true;
                }
                //Російські прізвища на ов, ев, єв
                else if (this.in(this.last(2), array("ов", "ев", "єв")) && !this.inNames(this.workingWord, array("Лев", "Остромов"))) {
                    this.wordForms(osnova, array(osLast + "а", osLast + "у", osLast + "а", osLast + "им", osLast + "у", this.inverse2(osLast) + "е"), 1);
                    this.rule(302);
                    return true;
                }
                //Російські прізвища на ін
                else if (this.in(this.last(2), array("ін"))) {
                    this.wordForms(this.workingWord, array("а", "у", "а", "ом", "у", "е"));
                    this.rule(303);
                    return true;
                }
                else {
                    this.wordForms(osnova, array(osLast + "а", osLast + "ові", osLast + "а", osLast + "ом", osLast + "ові", this.inverse2(osLast) + "е"), 1);
                    this.rule(304);
                    return true;
                }
            }
            if (group == 2) {
                //Мішана група
                this.wordForms(osnova, array("а", "еві", "а", "ем", "еві", "е"));
                this.rule(305);
                return true;
            }
            if (group == 3) {
                //М’яка група
                //Соловей
                if (Objects.equals(this.last(2), "ей") && this.in(this.last(3, 1), this.gubni)) {
                    osnova = substring(this.workingWord, 0, this.workingWord.length() - 2) + '’';
                    this.wordForms(osnova, array("я", "єві", "я", "єм", "єві", "ю"));
                    this.rule(306);
                    return true;
                }
                else if (Objects.equals(this.last(1), "й") || Objects.equals(beforeLast, "і")) {
                    this.wordForms(this.workingWord, array("я", "єві", "я", "єм", "єві", "ю"), 1);
                    this.rule(307);
                    return true;
                }
                //Швець
                else if (Objects.equals(this.workingWord, "швець")) {
                    this.wordForms(this.workingWord, array("евця", "евцеві", "евця", "евцем", "евцеві", "евцю"), 4);
                    this.rule(308);
                    return true;
                }
                //Слова що закінчуються на ець
                else if (Objects.equals(this.last(3), "ець")) {
                    this.wordForms(this.workingWord, array("ця", "цеві", "ця", "цем", "цеві", "цю"), 3);
                    this.rule(309);
                    return true;
                }
                //Слова що закінчуються на єць яць
                else if (this.in(this.last(3), array("єць", "яць"))) {
                    this.wordForms(this.workingWord, array("йця", "йцеві", "йця", "йцем", "йцеві", "йцю"), 3);
                    this.rule(310);
                    return true;
                }
                else {
                    this.wordForms(osnova, array("я", "еві", "я", "ем", "еві", "ю"));
                    this.rule(311);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Якщо слово закінчується на і, то відмінюємо як множину
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean manRule4() {
        if (Objects.equals(this.last(1), "і")) {
            this.wordForms(this.workingWord, array("их", "им", "их", "ими", "их", "і"), 1);
            this.rule(4);
            return true;
        }
        return false;
    }

    /**
     * Якщо слово закінчується на ий або ой
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean manRule5() {
        if (this.in(this.last(2), array("ий", "ой"))) {
            this.wordForms(this.workingWord, array("ого", "ому", "ого", "им", "ому", "ий"), 2);
            this.rule(5);
            return true;
        }
        return false;
    }

    /**
     * Українські чоловічі та жіночі імена, що в називному відмінку однини закінчуються на -а (-я),
     * відмінються як відповідні іменники І відміни.
     * - Примітка 1. Кінцеві приголосні основи г, к, х у жіночих іменах
     *   у давальному та місцевому відмінках однини перед закінченням -і
     *   змінюються на з, ц, с: Ольга - Ользі, Палажка - Палажці, Солоха - Солосі.
     * - Примітка 2. У жіночих іменах типу Одарка, Параска в родовому відмінку множини
     *   в кінці основи між приголосними з'являється звук о: Одарок, Парасок
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean womanRule1() {
        //Предпоследний символ
        String beforeLast = this.last(2, 1);

        //Якщо закінчується на ніга -» нога
        if (Objects.equals(this.last(4), "ніга")) {
            String osnova = substring(this.workingWord, 0, this.workingWord.length() - 3) + "о";
            this.wordForms(osnova, array("ги", "зі", "гу", "гою", "зі", "го"));
            this.rule(101);
            return true;
        }

        //Останні літера або а
        else if (Objects.equals(this.last(1), "а")) {
            this.wordForms(this.workingWord, array(beforeLast + "и", this.inverseGKH(beforeLast) + "і", beforeLast + "у", beforeLast + "ою", this.inverseGKH(beforeLast) + "і", beforeLast + "о"), 2);
            this.rule(102);
            return true;
        }
        //Остання літера я
        else if (Objects.equals(this.last(1), "я")) {

            if (this.in(beforeLast, this.vowels) || this.isApostrof(beforeLast)) {
                this.wordForms(this.workingWord, array("ї", "ї", "ю", "єю", "ї", "є"), 1);
                this.rule(103);
                return true;
            }
            else {
                this.wordForms(this.workingWord, array(beforeLast + "і", this.inverseGKH(beforeLast) + "і", beforeLast + "ю", beforeLast + "ею", this.inverseGKH(beforeLast) + "і", beforeLast + "е"), 2);
                this.rule(104);
                return true;
            }
        }
        return false;
    }

    /**
     * Українські жіночі імена, що в називному відмінку однини закінчуються на приголосний,
     * відмінюються як відповідні іменники ІІІ відміни
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean womanRule2() {
        if (this.in(this.last(1), this.consonant + "ь")) {
            String osnova = this.getOsnova(this.workingWord);
            String apostrof = "";
            String duplicate = "";
            String osLast = substring(osnova, -1, 1);
            String osbeforeLast = substring(osnova, -2, 1);

            //Чи треба ставити апостроф
            if (this.in(osLast, "мвпбф") && (this.in(osbeforeLast, this.vowels))) {
                apostrof = "’";
            }

            //Чи треба подвоювати
            if (this.in(osLast, "дтзсцлн")) {
                duplicate = osLast;
            }


            //Відмінюємо
            if (Objects.equals(this.last(1), "ь")) {
                this.wordForms(osnova, array("і", "і", "ь", duplicate + apostrof + "ю", "і", "е"));
                this.rule(201);
                return true;
            }
            else {
                this.wordForms(osnova, array("і", "і", "", duplicate + apostrof + "ю", "і", "е"));
                this.rule(202);
                return true;
            }
        }
        return false;
    }

    /**
     * Якщо слово на ськ або це російське прізвище
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean womanRule3() {
        //Предпоследний символ
        String beforeLast = this.last(2, 1);

        //Донская
        if (Objects.equals(this.last(2), "ая")) {
            this.wordForms(this.workingWord, array("ої", "ій", "ую", "ою", "ій", "ая"), 2);
            this.rule(301);
            return true;
        }

        //Ті що на ськ
        if (Objects.equals(this.last(1), "а") && (this.in(this.last(2, 1), "чнв") || this.in(this.last(3, 2), array("ьк")))) {
            this.wordForms(this.workingWord, array(beforeLast + "ої", beforeLast + "ій", beforeLast + "у", beforeLast + "ою", beforeLast + "ій", beforeLast + "о"), 2);
            this.rule(302);
            return true;
        }

        return false;
    }

    /**
     * Функція намагається застосувати ланцюг правил для чоловічих імен
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean manFirstName() {
        return this.rulesChain("man", array(1, 2, 3));
    }

    /**
     * Функція намагається застосувати ланцюг правил для жіночих імен
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean womanFirstName() {
        return this.rulesChain("woman", array(1, 2));
    }

    /**
     * Функція намагається застосувати ланцюг правил для чоловічих прізвищ
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean manSecondName() {
        return this.rulesChain("man", array(5, 1, 2, 3, 4));
    }

    /**
     * Функція намагається застосувати ланцюг правил для жіночих прізвищ
     * @return boolean true - якщо було задіяно правило з переліку, false - якщо правило не знайдено
     */
    protected boolean womanSecondName() {
        return this.rulesChain("woman", array(3, 1));
    }

    /**
     * Фунція відмінює чоловічі по-батькові
     * @return boolean true - якщо слово успішно змінене, false - якщо невдалося провідміняти слово
     */
    protected boolean manFatherName() {
        if (this.in(this.last(2), array("ич", "іч"))) {
            this.wordForms(this.workingWord, array("а", "у", "а", "ем", "у", "у"));
            return true;
        }
        return false;
    }

    /**
     * Фунція відмінює жіночі по-батькові
     * @return boolean true - якщо слово успішно змінене, false - якщо невдалося провідміняти слово
     */
    protected boolean womanFatherName() {
        if (this.in(this.last(3), array("вна"))) {
            this.wordForms(this.workingWord, array("и", "і", "у", "ою", "і", "о"), 1);
            return true;
        }
        return false;
    }

    /**
     * Визначення статі, за правилами імені
     * @param word об’єкт класу зі словом, для якого необхідно визначити стать
     */
    protected void genderByFirstName(NCLNameCaseWord word) {
        this.setWorkingWord(word.getWord());

        double man = 0; //Мужчина
        double woman = 0; //Женщина
        //Попробуем выжать максимум из имени
        //Если имя заканчивается на й, то скорее всего мужчина
        if (Objects.equals(this.last(1), "й")) {
            man+=0.9;
        }

        if (this.inNames(this.workingWord, array("Петро", "Микола"))) {
            man+=30;
        }

        if (this.in(this.last(2), array("он", "ов", "ав", "ам", "ол", "ан", "рд", "мп", "ко", "ло"))) {
            man+=0.5;
        }

        if (this.in(this.last(3), array("бов", "нка", "яра", "ила", "опа"))) {
            woman+=0.5;
        }

        if (this.in(this.last(1), this.consonant)) {
            man+=0.01;
        }

        if (Objects.equals(this.last(1), "ь")) {
            man+=0.02;
        }

        if (this.in(this.last(2), array("дь"))) {
            woman+=0.1;
        }

        if (this.in(this.last(3), array("ель", "бов"))) {
            woman+=0.4;
        }

        word.setGender(man, woman);
    }

    /**
     * Визначення статі, за правилами прізвища
     * @param word об’єкт класу зі словом, для якого необхідно визначити стать
     */
    protected void genderBySecondName(NCLNameCaseWord word) {
        this.setWorkingWord(word.getWord());

        double man = 0; //Мужчина
        double woman = 0; //Женщина

        if (this.in(this.last(2), array("ов", "ин", "ев", "єв", "ін", "їн", "ий", "їв", "ів", "ой", "ей"))) {
            man+=0.4;
        }

        if (this.in(this.last(3), array("ова", "ина", "ева", "єва", "іна", "мін"))) {
            woman+=0.4;
        }

        if (this.in(this.last(2), array("ая"))) {
            woman+=0.4;
        }

        word.setGender(man, woman);
    }

    /**
     * Визначення статі, за правилами по-батькові
     * @param word об’єкт класу зі словом, для якого необхідно визначити стать
     */
    protected void genderByFatherName(NCLNameCaseWord word) {
        this.setWorkingWord(word.getWord());

        if (Objects.equals(this.last(2), "ич")) {
            word.setGender(10, 0); // мужчина
        }
        if (Objects.equals(this.last(2), "на")) {
            word.setGender(0, 12); // женщина
        }
    }

    /**
     * Ідентифікує слово визначаючи чи це ім’я, чи це прізвище, чи це побатькові
     * - <b>N</b> - ім’я
     * - <b>S</b> - прізвище
     * - <b>F</b> - по-батькові
     * @param word об’єкт класу зі словом, яке необхідно ідентифікувати
     */
    protected void detectNamePart(NCLNameCaseWord word) {
        String namepart = word.getWord();
        this.setWorkingWord(namepart);

        //Считаем вероятность
        double first = 0;
        double second = 0;
        double father = 0;

        //если смахивает на отчество
        if (this.in(this.last(3), array("вна", "чна", "ліч")) || this.in(this.last(4), array("ьмич", "ович"))) {
            father+=3;
        }

        //Похоже на имя
        if (this.in(this.last(3), array("тин" /* {endings_sirname3} */)) || this.in(this.last(4), array("ьмич", "юбов", "івна", "явка", "орив", "кіян" /* {endings_sirname4} */))) {
            first+=0.5;
        }

        //Исключения
        if (this.inNames(namepart, array("Лев", "Гаїна", "Афіна", "Антоніна", "Ангеліна", "Альвіна", "Альбіна", "Аліна", "Павло", "Олесь", "Микола", "Мая", "Англеліна", "Елькін", "Мерлін"))) {
            first+=10;
        }

        //похоже на фамилию
        if (this.in(this.last(2), array("ов", "ін", "ев", "єв", "ий", "ин", "ой", "ко", "ук", "як", "ца", "их", "ик", "ун", "ок", "ша", "ая", "га", "єк", "аш", "ив", "юк", "ус", "це", "ак", "бр", "яр", "іл", "ів", "ич", "сь", "ей", "нс", "яс", "ер", "ай", "ян", "ах", "ць", "ющ", "іс", "ач", "уб", "ох", "юх", "ут", "ча", "ул", "вк", "зь", "уц", "їн", "де", "уз", "юр", "ік", "іч", "ро" /* {endings_name2} */))) {
            second+=0.4;
        }

        if (this.in(this.last(3), array("ова", "ева", "єва", "тих", "рик", "вач", "аха", "шен", "мей", "арь", "вка", "шир", "бан", "чий", "іна", "їна", "ька", "ань", "ива", "аль", "ура", "ран", "ало", "ола", "кур", "оба", "оль", "нта", "зій", "ґан", "іло", "шта", "юпа", "рна", "бла", "еїн", "има", "мар", "кар", "оха", "чур", "ниш", "ета", "тна", "зур", "нір", "йма", "орж", "рба", "іла", "лас", "дід", "роз", "аба", "чан", "ган" /* {endings_name3} */))) {
            second+=0.4;
        }

        if (this.in(this.last(4), array("ьник", "нчук", "тник", "кирь", "ский", "шена", "шина", "вина", "нина", "гана", "гана", "хній", "зюба", "орош", "орон", "сило", "руба", "лест", "мара", "обка", "рока", "сика", "одна", "нчар", "вата", "ндар", "грій" /* {endings_name4} */))) {
            second+=0.4;
        }

        if (Objects.equals(this.last(1), "і")) {
            second+=0.2;
        }

        Double max = array(first, second, father).stream().max(Double::compareTo).get();

        if (max.equals(first)) {
            word.setNamePart(N);
        }
        else if (max.equals(second)) {
            word.setNamePart(S);
        }
        else {
            word.setNamePart(F);
        }
    }

}