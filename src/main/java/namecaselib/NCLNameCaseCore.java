package namecaselib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static namecaselib.NCL.Gender.MAN;
import static namecaselib.NCL.Gender.WOMAN;
import static namecaselib.NCL.NamePart.F;
import static namecaselib.NCL.NamePart.N;
import static namecaselib.NCL.NamePart.S;

/**
 * <b>NCL NameCase Core</b>
 *
 * Набор основных функций, который позволяют сделать интерфейс слонения русского и украниского языка
 * абсолютно одинаковым. Содержит все функции для внешнего взаимодействия с библиотекой.
 *
 */
public abstract class NCLNameCaseCore extends NCL {

	/**
	 * Версия библиотеки
	 */
	protected String version = "0.4.1";
	/**
	 * Версия языкового файла
	 */
	protected String languageBuild = "0";
	/**
	 * Готовность системы:
	 * - Все слова идентифицированы (известо к какой части ФИО относится слово)
	 * - У всех слов определен пол
	 * Если все сделано стоит флаг true, при добавлении нового слова флаг сбрасывается на false
	 */
	private boolean ready = false;
	/**
	 * Если все текущие слова было просклонены и в каждом слове уже есть результат склонения,
	 * тогда true. Если было добавлено новое слово флаг збрасывается на false
	 */
	private boolean finished = false;
	/**
	 * Массив содержит елементы типа NCLNameCaseWord. Это все слова которые нужно обработать и просклонять
	 * @var array
	 */
	private List<NCLNameCaseWord> words = new ArrayList<>();
	/**
	 * Переменная, в которую заносится слово с которым сейчас идет работа
	 * @var string
	 */
	protected String workingWord = "";
	/**
	 * Метод Last() вырезает подстроки разной длины. Посколько одинаковых вызовов бывает несколько,
	 * то все результаты выполнения кешируются в этом массиве.
	 * @var array
	 */
	protected Map<Integer, Map<Integer, String>> workindLastCache = new HashMap<>();
	/**
	 * Номер последнего использованого правила, устанавливается методом Rule()
	 * @var int
	 */
	private int lastRule = 0;
	/**
	 * Массив содержит результат склонения слова - слово во всех падежах
	 * @var array
	 */
	protected List<String> lastResult = new ArrayList<>();
	/**
	 * Массив содержит информацию о том какие слова из массива <var>this.words</var> относятся к
	 * фамилии, какие к отчеству а какие к имени. Массив нужен потому, что при добавлении слов мы не
	 * всегда знаем какая часть ФИО сейчас, поэтому после идентификации всех слов генерируется массив
	 * индексов для быстрого поиска в дальнейшем.
	 * @var array
	 */
	private Map<NamePart, List<Integer>> index = new HashMap<>();

	public double genderKoef = 0;//вероятность автоопредления пола [0..10]. Достаточно точно при 0.1

	/**
	 * Метод очищает результаты последнего склонения слова. Нужен при склонении нескольких слов.
	 */
	private void reset() {
		this.lastRule = 0;
		this.lastResult = new ArrayList<>();
	}

	/**
	 * Сбрасывает все информацию на начальную. Очищает все слова добавленые в систему.
	 * После выполнения система готова работать с начала.
	 * @return NCLNameCaseCore
	 */
	public NCLNameCaseCore fullReset() {
		this.words = new ArrayList<>();;
		this.index = new HashMap<>();
		this.index.put(N, new ArrayList<>());
		this.index.put(F, new ArrayList<>());
		this.index.put(S, new ArrayList<>());
		this.reset();
		this.notReady();
		return this;
	}

	/**
	 * Устанавливает флаги о том, что система не готово и слова еще не были просклонены
	 */
	private void notReady() {
		this.ready = false;
		this.finished = false;
	}

	/**
	 * Устанавливает номер последнего правила
	 * @param index номер правила которое нужно установить
	 */
	protected void rule(int index) {
		this.lastRule = index;
	}

	/**
	 * Устанавливает слово текущим для работы системы. Очищает кеш слова.
	 * @param word слово, которое нужно установить
	 */
	protected void setWorkingWord(String word) {
		//Сбрасываем настройки
		this.reset();
		//Ставим слово
		this.workingWord = word;
		//Чистим кеш
		this.workindLastCache = new HashMap<>();
	}

	/**
	 * Если не нужно склонять слово, делает результат таким же как и именительный падеж
	 */
	protected void makeResultTheSame() {
		List<String> array = new ArrayList<>();
		for (int i = 0; i < this.getCaseCount(); i++) {
			array.add(this.workingWord);
		}
		this.lastResult = array;
	}

	protected String last(int length) {
		return last(length, 0);
	}

	/**
	 * Если <var>stopAfter</var> = 0, тогда вырезает length последних букв с текущего слова (<var>this.workingWord</var>)
	 * Если нет, тогда вырезает <var>stopAfter</var> букв начиная от <var>length</var> с конца
	 * @param length количество букв с конца
	 * @param stopAfter количество букв которые нужно вырезать (0 - все)
	 * @return string требуемая подстрока
	 */
	protected String last(int length, int stopAfter) {
		int cut = 0;
		//Сколько букв нужно вырезать все или только часть
		if (stopAfter == 0) {
			cut = length;
		} else {
			cut = stopAfter;
		}

		//Проверяем кеш
		if (!(this.workindLastCache.containsKey(length) && this.workindLastCache.get(length).containsKey(stopAfter))) {
			this.workindLastCache.computeIfAbsent(length, (key) -> new HashMap<>());
			//this.workindLastCache[length][stopAfter] = NCLStr::substr(this.workingWord, -length, cut);
			String substr = substring(this.workingWord, -1 * length, cut);
			this.workindLastCache.get(length).put(stopAfter, substr);
		}
		return this.workindLastCache.get(length).get(stopAfter);
	}

	/**
	 * Над текущим словом (<var>this.workingWord</var>) выполняются правила в порядке указаном в <var>rulesArray</var>.
	 * <var>gender</var> служит для указания какие правила использовать мужские ('man') или женские ('woman')
	 * @param gender - префикс мужских/женских правил
	 * @param rulesArray - массив, порядок выполнения правил
	 * @return boolean если правило было задествовано, тогда true, если нет - тогда false
	 */
	protected boolean rulesChain(String gender, List<Integer> rulesArray) {
		for (int ruleId: rulesArray) {
			String ruleMethod = gender + "Rule"  + ruleId;
			if (this.ruleMethod(ruleMethod)) {
				return true;
			}
		}
		return false;
	}

	protected abstract boolean ruleMethod(String ruleMethod);

	protected boolean in(String letter, String string) {
		return letter != null && !letter.isEmpty() && string.contains(letter);
	}

	protected boolean in(String letter, List<String> strings) {
		return letter != null && !letter.isEmpty() && strings.contains(letter);
	}

	/**
	 * Функция проверяет, входит ли имя <var>nameNeedle</var> в перечень имен <var>names</var>.
	 * @param nameNeedle - имя которое нужно найти
	 * @param names - перечень имен в котором нужно найти имя
	 */
	protected boolean inNames(String nameNeedle, List<String> names) {
		for(String name: names) {
			if (nameNeedle.toLowerCase().equals(name.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Склоняет слово <var>word</var>, удаляя из него <var>replaceLast</var> последних букв
	 * и добавляя в каждый падеж окончание из массива <var>endings</var>.
	 * @param word слово, к которому нужно добавить окончания
	 * @param endings массив окончаний
	 * @param replaceLast сколько последних букв нужно убрать с начального слова
	 */
	protected void wordForms(String word, List<String> endings, int replaceLast) {
		//Создаем массив с именительный падежом
		List<String> result = new ArrayList<>();
		result.add(this.workingWord);
		//Убираем в окончание лишние буквы
		word = substring(word, 0, word.length() - replaceLast);

		//Добавляем окончания
		for (int i = 1; i < this.getCaseCount(); i++) {
			result.add(word + endings.get(i - 1));
		}

		this.lastResult = result;
	}

	protected void wordForms(String word, List<String> endings) {
		wordForms(word, endings, 0);
	}

	public abstract int getCaseCount();

	/**
	 * В массив <var>this.words</var> добавляется новый об’єкт класса NCLNameCaseWord
	 * со словом <var>firstname</var> и пометкой, что это имя
	 * @param firstname имя
	 * @return NCLNameCaseCore
	 */
	public NCLNameCaseCore setFirstName(String firstname) {
		if (isNotEmpty(firstname)) {
			NCLNameCaseWord nameCaseWord = new NCLNameCaseWord(firstname);
			this.words.add(nameCaseWord);
			nameCaseWord.setNamePart(N);
			this.notReady();
		}
		return this;
	}

	/**
	 * В массив <var>this.words</var> добавляется новый об’єкт класса NCLNameCaseWord
	 * со словом <var>secondname</var> и пометкой, что это фамилия
	 * @param secondname фамилия
	 * @return NCLNameCaseCore
	 */
	public NCLNameCaseCore setLastName(String secondname) {
		if (isNotEmpty(secondname)) {
			NCLNameCaseWord nameCaseWord = new NCLNameCaseWord(secondname);
			this.words.add(nameCaseWord);
			nameCaseWord.setNamePart(S);
			this.notReady();
		}
		return this;
	}

	/**
	 * В массив <var>this.words</var> добавляется новый об’єкт класса NCLNameCaseWord
	 * со словом <var>fathername</var> и пометкой, что это отчество
	 * @param fathername отчество
	 * @return NCLNameCaseCore
	 */
	public NCLNameCaseCore setFatherName(String fathername) {
		if (isNotEmpty(fathername)) {
			NCLNameCaseWord nameCaseWord = new NCLNameCaseWord(fathername);
			this.words.add(nameCaseWord);
			nameCaseWord.setNamePart(F);
			this.notReady();
		}
		return this;
	}

	/**
	 * Всем словам устанавливается пол, который может иметь следующие значения
	 * - null - не определено
	 * - NCL.Gender.MAN - мужчина
	 * - NCL.Gender.WOMAN - женщина
	 * @param gender пол, который нужно установить
	 * @return NCLNameCaseCore
	 */
	public NCLNameCaseCore setGender(Gender gender) {
		for(NCLNameCaseWord word: this.words) {
			word.setTrueGender(gender);
		}
		return this;
	}

	/**
	 * В система заносится сразу фамилия, имя, отчество
	 * @param secondName фамилия
	 * @param firstName имя
	 * @param fatherName отчество
	 * @return NCLNameCaseCore
	 */
	public NCLNameCaseCore setFullName(String secondName, String firstName, String fatherName) {
		this.setFirstName(firstName);
		this.setLastName(secondName);
		this.setFatherName(fatherName);
		return this;
	}

	/**
	 * Если слово <var>word</var> не идентифицировано, тогда определяется это имя, фамилия или отчество
	 * @param word слово которое нужно идентифицировать
	 */
	private void prepareNamePart(NCLNameCaseWord word) {
		if (word.getNamePart() == null) {
			this.detectNamePart(word);
		}
	}

	/**
	 * Проверяет все ли слова идентифицированы, если нет тогда для каждого определяется это имя, фамилия или отчество
	 */
	private void prepareAllNameParts() {
		for(NCLNameCaseWord word: this.words) {
			this.prepareNamePart(word);
		}
	}

	/**
	 * Определяет пол для слова <var>word</var>
	 * @param word слово для которого нужно определить пол
	 */
	private void prepareGender(NCLNameCaseWord word) {
		if (!word.isGenderSolved()) {
			NamePart namePart = word.getNamePart();
			switch (namePart) {
				case N: this.genderByFirstName(word);
					break;
				case F: this.genderByFatherName(word);
					break;
				case S: this.genderBySecondName(word);
					break;
			}
		}
	}

	/**
	 * Для всех слов проверяет определен ли пол, если нет - определяет его
	 * После этого расчитывает пол для всех слов и устанавливает такой пол всем словам
	 * @return bool был ли определен пол
	 */
	private boolean solveGender() {
		//Ищем, может гдето пол уже установлен
		for (NCLNameCaseWord word: this.words) {
			if (word.isGenderSolved()) {
				this.setGender(word.gender());
				return true;
			}
		}

		//Если нет тогда определяем у каждого слова и потом сумируем
		int man = 0;
		int woman = 0;

		for (NCLNameCaseWord word: this.words) {
			this.prepareGender(word);
			Map<Gender, Double> gender = word.getGender();
			man += gender.get(MAN);
			woman += gender.get(WOMAN);
		}

		if (man > woman) {
			this.setGender(MAN);
		} else {
			this.setGender(WOMAN);
		}

		return true;
	}

	/**
	 * Генерируется массив, который содержит информацию о том какие слова из массива <var>this.words</var> относятся к
	 * фамилии, какие к отчеству а какие к имени. Массив нужен потому, что при добавлении слов мы не
	 * всегда знаем какая часть ФИО сейчас, поэтому после идентификации всех слов генерируется массив
	 * индексов для быстрого поиска в дальнейшем.
	 */
	private void generateIndex() {
		this.index = new HashMap<>();
		this.index.put(N, new ArrayList<>());
		this.index.put(F, new ArrayList<>());
		this.index.put(S, new ArrayList<>());

		for (int i = 0; i < this.words.size(); i++) {
			NCLNameCaseWord word = this.words.get(i);
			NamePart namepart = word.getNamePart();
			this.index.get(namepart).add(i);
		}
	}

	/**
	 * Выполнет все необходимые подготовления для склонения.
	 * Все слова идентфицируются. Определяется пол.
	 * Обновляется индекс.
	 */
	private void prepareEverything() {
		if (!this.ready) {
			this.prepareAllNameParts();
			this.solveGender();
			this.generateIndex();
			this.ready = true;
		}
	}

	/**
	 * По указаным словам определяется пол человека:
	 * - null - не определено
	 * - NCL::MAN - мужчина
	 * - NCL::WOMAN - женщина
	 * @return int текущий пол человека
	 */
	public Gender genderAutoDetect() {
		this.prepareEverything();

		if (!this.words.isEmpty()){
			int n = -1;
			double maxKoef = -1;
			for (int k = 0; k < this.words.size(); k++) {
				NCLNameCaseWord word = this.words.get(k);
				Map<Gender, Double> genders = word.getGender();
				// TODO check nullability
				double min = genders.values().stream().min(Double::compareTo).get();
				double max = genders.values().stream().max(Double::compareTo).get();

				double koef = max - min;
				if (koef > maxKoef) {
					maxKoef=koef;
					n=k;
				}
			}

			if (n >= 0){
				if (this.words.size() > n) {
					NCLNameCaseWord word = this.words.get(n);
					Map<Gender, Double> genders = word.getGender();
					double min = genders.values().stream().min(Double::compareTo).get();
					double max = genders.values().stream().max(Double::compareTo).get();
					this.genderKoef = max - min;
					return word.gender();
				}
			}
		}
		return null;
	}

	/**
	 * Разбивает строку <var>fullname</var> на слова и возвращает формат в котором записано имя
	 * <b>Формат:</b>
	 * - S - Фамилия
	 * - N - Имя
	 * - F - Отчество
	 * @param fullname строка, для которой необходимо определить формат
	 * @return array формат в котором записано имя массив типа <var>this.words</var>
	 */
	private List<NCLNameCaseWord> splitFullName(String fullname) {

		fullname = fullname.trim();
		List<String> list = Arrays.asList(fullname.split(" "));

		for (String word: list) {
			this.words.add(new NCLNameCaseWord(word));
		}

		this.prepareEverything();
		return this.words;
	}

	/**
	 * Разбивает строку <var>fullname</var> на слова и возвращает формат в котором записано имя
	 * <b>Формат:</b>
	 * - S - Фамилия
	 * - N - Имя
	 * - F - Отчество
	 * @param fullname строка, для которой необходимо определить формат
	 * @return string формат в котором записано имя
	 */
	public String getFullNameFormat(String fullname) {
		this.fullReset();
		words = this.splitFullName(fullname);
		StringBuilder format = new StringBuilder();
		for (NCLNameCaseWord word: words) {
			format.append(word.getNamePart()).append(" ");
		}
		return format.toString();
	}

	/**
	 * Склоняет слово <var>word</var> по нужным правилам в зависимости от пола и типа слова
	 * @param word слово, которое нужно просклонять
	 */
	private void wordCase(NCLNameCaseWord word) {

		Supplier<Boolean> method = null;

		NamePart namePartLetter = word.getNamePart();
		switch (namePartLetter) {
			case F:
				method = word.gender() == MAN ? this::manFatherName : this::womanFatherName;
				break;
			case N:
				method = word.gender() == MAN ? this::manFirstName : this::womanFirstName;
				break;
			case S:
				method = word.gender() == MAN ? this::manSecondName : this::womanSecondName;
				break;
		}

		this.setWorkingWord(word.getWord());

		if (method.get()) {
			word.setNameCases(this.lastResult);
			word.setRule(this.lastRule);
		} else {
			word.setNameCases(array_fill(this.getCaseCount(), word.getWord()));
			word.setRule(-1);
		}
	}

	/**
	 * Производит склонение всех слов, который хранятся в массиве <var>this.words</var>
	 */
	private void allWordCases() {
		if (!this.finished) {
			this.prepareEverything();
			for (NCLNameCaseWord word : this.words) {
				this.wordCase(word);
			}
			this.finished = true;
		}
	}

	/**
	 * Если указан номер падежа <var>number</var>, тогда возвращается строка с таким номером падежа,
	 * если нет, тогда возвращается массив со всеми падежами текущего слова.
	 * @param word слово для котрого нужно вернуть падеж
	 * @param number номер падежа, который нужно вернуть
	 */
	private String getWordCase(NCLNameCaseWord word, int number) {
		return word.getNameCases().get(number);
	}

	private List<String> getWordCase(NCLNameCaseWord word) {
		return word.getNameCases();
	}

	/**
	 * Если нужно было просклонять несколько слов, то их необходимо собрать в одну строку.
	 * Эта функция собирает все слова указаные в <var>indexArray</var>  в одну строку.
	 * @param indexArray индексы слов, которые необходимо собрать вместе
	 * @param number номер падежа
	 * @return mixed либо массив со всеми падежами, либо строка с одним падежом
	 */
	private List<String> getCasesConnected(List<Integer> indexArray, int number) {
		List<String> readyArr = array();
		for(int index: indexArray) {
			readyArr.add(this.getWordCase(this.words.get(index), number));
		}
		return readyArr;
	}

	private List<String> getCasesConnected(List<Integer> indexArray) {
		List<List<String>> readyArr = array();
		for(int index: indexArray) {
			readyArr.add(this.getWordCase(this.words.get(index)));
		}

		//Масив нужно скелить каждый падеж
		List<String> resultArr = array();
		for (int c = 0; c < this.getCaseCount(); c++) {
			List<String> tmp = array();
			for (int i = 0; i < readyArr.size(); i++) {
				tmp.add(readyArr.get(i).get(c));
			}
			resultArr.add(String.join(" ", tmp));
		}
		return resultArr;

	}


	/**
	 * Функция ставит имя в нужный падеж.
	 *
	 * Если указан номер падежа <var>number</var>, тогда возвращается строка с таким номером падежа,
	 * если нет, тогда возвращается массив со всеми падежами текущего слова.
	 */
	public List<String> getFirstNameCase() {
		this.allWordCases();
		return this.getCasesConnected(this.index.get(N));
	}

	/**
	 * Функция ставит фамилию в нужный падеж.
	 *
	 * Если указан номер падежа <var>number</var>, тогда возвращается строка с таким номером падежа,
	 * если нет, тогда возвращается массив со всеми падежами текущего слова.
	 */
	public List<String> getSecondNameCase() {
		this.allWordCases();
		return this.getCasesConnected(this.index.get(S));
	}

	/**
	 * Функция ставит отчество в нужный падеж.
	 *
	 * Если указан номер падежа <var>number</var>, тогда возвращается строка с таким номером падежа,
	 * если нет, тогда возвращается массив со всеми падежами текущего слова.
	 */
	public List<String> getFatherNameCase() {
		this.allWordCases();
		return this.getCasesConnected(this.index.get(F));
	}

	/**
	 * Функция ставит имя <var>firstName</var> в нужный падеж <var>CaseNumber</var> по правилам пола <var>gender</var>.
	 *
	 * Если указан номер падежа <var>CaseNumber</var>, тогда возвращается строка с таким номером падежа,
	 * если нет, тогда возвращается массив со всеми падежами текущего слова.
	 * @param firstName имя, которое нужно просклонять
	 * @param caseNumber номер падежа
	 * @param gender пол, который нужно использовать
	 * @return mixed массив или строка с нужным падежом
	 */
	public String qFirstName(String firstName, int caseNumber, Gender gender) {
		this.fullReset();
		this.setFirstName(firstName);
		this.setGender(gender);
		return this.getFirstNameCase().get(caseNumber);
	}

	/**
	 * Функция ставит фамилию <var>secondName</var> в нужный падеж <var>CaseNumber</var> по правилам пола <var>gender</var>.
	 *
	 * Если указан номер падежа <var>CaseNumber</var>, тогда возвращается строка с таким номером падежа,
	 * если нет, тогда возвращается массив со всеми падежами текущего слова.
	 * @param secondName фамилия, которую нужно просклонять
	 * @param caseNumber номер падежа
	 * @param gender пол, который нужно использовать
	 * @return mixed массив или строка с нужным падежом
	 */
	public String qSecondName(String secondName, int caseNumber, Gender gender) {
		this.fullReset();
		this.setLastName(secondName);
		this.setGender(gender);
		return this.getSecondNameCase().get(caseNumber);
	}

	/**
	 * Функция ставит отчество <var>fatherName</var> в нужный падеж <var>CaseNumber</var> по правилам пола <var>gender</var>.
	 *
	 * Если указан номер падежа <var>CaseNumber</var>, тогда возвращается строка с таким номером падежа,
	 * если нет, тогда возвращается массив со всеми падежами текущего слова.
	 * @param fatherName отчество, которое нужно просклонять
	 * @param caseNumber номер падежа
	 * @param gender пол, который нужно использовать
	 * @return mixed массив или строка с нужным падежом
	 */
	public String qFatherName(String fatherName, int caseNumber, Gender gender) {
		this.fullReset();
		this.setFatherName(fatherName);
		this.setGender(gender);
		return this.getFatherNameCase().get(caseNumber);
	}

	/**
	 * Определяет пол человека по ФИО
	 * @param fullname ФИО
	 * @return int пол человека
	 */
	public Gender genderDetect(String fullname) {
		this.fullReset();
		this.splitFullName(fullname);
		return this.genderAutoDetect();
	}

	/**
	 * Возвращает внутренний массив this.words каждая запись имеет тип NCLNameCaseWord
	 * @return array Массив всех слов в системе
	 */
	public List<NCLNameCaseWord> getWordsArray() {
		return this.words;
	}

	/**
	 * Функция пытается применить цепочку правил для мужских имен
	 * @return boolean true - если было использовано правило из списка, false - если правило не было найденым
	 */
	protected boolean manFirstName() {
		return false;
	}

	/**
	 * Функция пытается применить цепочку правил для женских имен
	 * @return boolean true - если было использовано правило из списка, false - если правило не было найденым
	 */
	protected boolean womanFirstName() {
		return false;
	}

	/**
	 * Функция пытается применить цепочку правил для мужских фамилий
	 * @return boolean true - если было использовано правило из списка, false - если правило не было найденым
	 */
	protected boolean manSecondName() {
		return false;
	}

	/**
	 * Функция пытается применить цепочку правил для женских фамилий
	 * @return boolean true - если было использовано правило из списка, false - если правило не было найденым
	 */
	protected boolean womanSecondName() {
		return false;
	}

	/**
	 * Функция склоняет мужский отчества
	 * @return boolean true - если слово было успешно изменено, false - если не получилось этого сделать
	 */
	protected boolean manFatherName() {
		return false;
	}

	/**
	 * Функция склоняет женские отчества
	 * @return boolean true - если слово было успешно изменено, false - если не получилось этого сделать
	 */
	protected boolean womanFatherName() {
		return false;
	}

	/**
	 * Определение пола по правилам имен
	 * @param word word обьект класса слов, для которого нужно определить пол
	 */
	protected abstract void genderByFirstName(NCLNameCaseWord word);

	/**
	 * Определение пола по правилам фамилий
	 * @param word word обьект класса слов, для которого нужно определить пол
	 */
	protected abstract void genderBySecondName(NCLNameCaseWord word);

	/**
	 * Определение пола по правилам отчеств
	 * @param word word обьект класса слов, для которого нужно определить пол
	 */
	protected abstract void genderByFatherName(NCLNameCaseWord word);

	/**
	 * Идетифицирует слово определяе имя это, или фамилия, или отчество
	 * - <b>N</b> - имя
	 * - <b>S</b> - фамилия
	 * - <b>F</b> - отчество
	 * @param word обьект класса слов, который необходимо идентифицировать
	 */
	protected abstract void detectNamePart(NCLNameCaseWord word);

	/**
	 * Возвращает версию библиотеки
	 * @return string версия библиотеки
	 */
	public String version() {
		return this.version;
	}

	/**
	 * Возвращает версию использованого языкового файла
	 * @return string версия языкового файла
	 */
	public String languageVersion() {
		return this.languageBuild;
	}

}
