package namecaselib;

import namecaselib.NCL.Gender;
import namecaselib.NCL.NamePart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static namecaselib.NCL.substring;

/**
 * NCLNameCaseWord - класс, который служит для хранения всей информации о каждом слове
 */
public class NCLNameCaseWord {

	/**
	 * Слово в нижнем регистре, которое хранится в об’єкте класса
	 */
	private String word = "";

	/**
	 * Оригинальное слово
	 */
	private String wordOrig = "";

	/**
	 * Тип текущей записи (Фамилия/Имя/Отчество)
	 * - <b>N</b> - ім’я
	 * - <b>S</b> - прізвище
	 * - <b>F</b> - по-батькові
	 */
	private NamePart namePart = null;

	/**
	 * Вероятность того, что текущей слово относится к мужскому полу
	 */
	public double genderMan = 0;

	/**
	 * Вероятность того, что текущей слово относится к женскому полу
	 */
	public double genderWoman = 0;

	/**
	 * Окончательное решение, к какому полу относится слово
	 * - 0 - не определено
	 * - NCL.MAN - мужской пол
	 * - NCL.WOMAN - женский пол
	 */
	public Gender genderSolved = null;

	/**
	 * Маска больших букв в слове.
	 *
	 * Содержит информацию о том, какие буквы в слове были большими, а какие мальникими:
	 * - x - маленькая буква
	 * - X - больная буква
	 * @var array
	 */
	private List<Character> letterMask = new ArrayList<>();

	/**
	 * Содержит true, если все слово было в верхнем регистре и false, если не было
	 */
	public boolean isUpperCase = false;

	/**
	 * Массив содержит все падежи слова, полученые после склонения текущего слова
	 * @var array
	 */
	private List<String> nameCases = new ArrayList<>();

	/**
	 * Номер правила, по которому было произведено склонение текущего слова
	 */
	private int rule = 0;

	/**
	 * Создание нового обьекта со словом
	 */
	public NCLNameCaseWord(String word) {
		this.wordOrig = word;
		this.letterMask = this.generateMask(word);
		this.word = word.toLowerCase();
	}

	/**
	 * Генерирует маску, которая содержит информацию о том, какие буквы в слове были большими, а какие маленькими:
	 * - x - маленькая буква
	 * - X - больная буква
	 */
	private List<Character> generateMask(String word) {
		List<Character> mask = new ArrayList<>();
		this.isUpperCase = true;
		for (char letter: word.toCharArray()) {
			if (Character.isLowerCase(letter)) {
				mask.add('x');
				this.isUpperCase = false;
			} else {
				mask.add('X');
			}
		}
		return mask;
	}

	/**
	 * Возвращает все падежи слова в начальную маску:
	 * - x - маленькая буква
	 * - X - больная буква
	 */
	private void returnMask() {
		if (this.isUpperCase) {
			this.nameCases = this.nameCases.stream().map(String::toUpperCase).collect(toList());
		} else {
			List<Character> splitedMask = this.letterMask;
			int maskLength = splitedMask.size();
			for (int index = 0; index < this.nameCases.size(); index++) {
				String nameCase = this.nameCases.get(index);
				int caseLength = nameCase.length();
				// origin code: $max = min(array($caseLength, $maskLength));
				int max = Math.min(caseLength, maskLength);
				StringBuilder newNameCase = new StringBuilder();
				for (int letterIndex = 0; letterIndex < max; letterIndex++) {
					char letter = nameCase.charAt(letterIndex);
					if (splitedMask.get(letterIndex).equals('X')) {
						letter = Character.toUpperCase(letter);
					}
					newNameCase.append(letter);
				}
				newNameCase.append(substring(nameCase, max, caseLength - maskLength));
				this.nameCases.set(index, newNameCase.toString());
			}
		}
	}

	/**
	 * Сохраняет результат склонения текущего слова
	 */
	public void setNameCases(List<String> nameCases, Boolean isReturnMask) {
		this.nameCases = nameCases;
		if (isReturnMask) {
			this.returnMask();
		}
	}

	/**
	 * Сохраняет результат склонения текущего слова
	 */
	public void setNameCases(List<String> nameCases) {
		this.nameCases = nameCases;
		this.returnMask();
	}

	/**
	 * Возвращает массив со всеми падежами текущего слова
	 * @return array массив со всеми падежами
	 */
	public List<String> getNameCases() {
		return this.nameCases;
	}

	/**
	 * Возвращает строку с нужным падежом текущего слова
	 * @param number нужный падеж
	 * @return string строка с нужным падежом текущего слова
	 */
	public String getNameCase(int number) {
		if (this.nameCases.size() > number) {
			return this.nameCases.get(number);
		} else {
			return null;
		}
	}

	/**
	 * Расчитывает и возвращает пол текущего слова
	 * @return int пол текущего слова
	 */
	public Gender gender() {
		if (this.genderSolved == null) {
			if (this.genderMan >= this.genderWoman) {
				this.genderSolved = Gender.MAN;
			} else {
				this.genderSolved = Gender.WOMAN;
			}
		}
		return this.genderSolved;
	}

	/**
	 * Устанавливает вероятности того, что даное слово является мужчиной или женщиной
	 * @param man вероятность того, что слово мужчина
	 * @param woman верятность того, что слово женщина
	 */
	public void setGender(double man, double woman) {
		this.genderMan = man;
		this.genderWoman = woman;
	}

	/**
	 * Окончательно устанавливает пол человека
	 * - null - не определено
	 * - NCL.MAN - мужчина
	 * - NCL.WOMAN - женщина
	 * @param gender пол человека
	 */
	public void setTrueGender(Gender gender) {
		this.genderSolved = gender;
	}

	/**
	 * Возвращает массив вероятности того, что даное слово является мужчиной или женщиной
	 * @return array массив вероятностей
	 */
	public Map<Gender, Double> getGender() {
		Map<Gender, Double> gender = new HashMap<>();
		gender.put(Gender.MAN, this.genderMan);
		gender.put(Gender.WOMAN, this.genderWoman);
		return gender;
	}

	/**
	 * Устанавливает тип текущего слова
	 * <b>Тип слова:</b>
	 * - S - Фамилия
	 * - N - Имя
	 * - F - Отчество
	 * @param namePart тип слова
	 */
	public void setNamePart(NamePart namePart) {
		this.namePart = namePart;
	}

	/**
	 * Возвращает тип текущего слова
	 * <b>Тип слова:</b>
	 * - S - Фамилия
	 * - N - Имя
	 * - F - Отчество
	 * @return string $namePart тип слова
	 */
	public NamePart getNamePart() {
		return this.namePart;
	}

	/**
	 * Возвращает текущее слово.
	 * @return string текущее слово
	 */
	public String getWord() {
		return this.word;
	}

	/**
	 * Возвращает текущее оригинальное слово.
	 * @return string текущее слово
	 */
	public String getWordOrig() {
		return this.wordOrig;
	}

	/**
	 * Если уже был расчитан пол для всех слов системы, тогда каждому слову предается окончательное
	 * решение. Эта функция определяет было ли принято окончательное решение.
	 * @return bool было ли принято окончательное решение по поводу пола текущего слова
	 */
	public boolean isGenderSolved() {
		return this.genderSolved != null;
	}

	/**
	 * Устанавливает номер правила по которому склонялось текущее слово.
	 * @param ruleId номер правила
	 */
	public void setRule(int ruleId) {
		this.rule = ruleId;
	}
}
