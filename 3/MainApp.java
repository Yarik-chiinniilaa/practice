import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Клас для зберігання параметрів і результатів обчислень потенційної енергії.
 * Реалізує інтерфейс {@link Serializable} для збереження стану об'єктів.
 * @author Студент
 * @version 1.2
 */
class CalculationResult implements Serializable {
    /** Унікальний ідентифікатор версії серіалізації. */
    private static final long serialVersionUID = 1L;
    
    /** Маса тіла в кілограмах. */
    public double mass;
    /** Висота підйому в метрах. */
    public double height;
    /** Обчислена потенційна енергія в Джоулях. */
    public double energy;
    /** Максимальна кількість послідовних одиниць у бінарному представленні значення енергії. */
    public int maxConsecutiveOnes;

    /**
     * Конструктор для ініціалізації всіх полів результату.
     * @param mass маса тіла
     * @param height висота підйому
     * @param energy обчислена енергія
     * @param maxOnes результат бітового аналізу (макс. послідовність одиниць)
     */
    public CalculationResult(double mass, double height, double energy, int maxOnes) {
        this.mass = mass;
        this.height = height;
        this.energy = energy;
        this.maxConsecutiveOnes = maxOnes;
    }

    /**
     * Перевизначає метод toString для текстового представлення об'єкта.
     * Демонструє механізм заміщення (Overriding).
     * @return форматований рядок з параметрами та результатами
     */
    @Override
    public String toString() {
        return String.format("Параметри: [m=%.2f, h=%.2f] | Енергія: %.2f | Макс. посл. 1-ць: %d", 
                mass, height, energy, maxConsecutiveOnes);
    }
}

/**
 * Інтерфейс для об'єктів, що відповідають за візуалізацію результатів.
 * Є частиною шаблону Factory Method (продукт).
 */
interface ResultViewer {
    /**
     * Виводить список результатів у консоль.
     * @param results список об'єктів {@link CalculationResult} для відображення
     */
    void printResults(List<CalculationResult> results);
}

/**
 * Базова реалізація візуалізатора, що виводить дані у вигляді простого списку.
 */
class TextResultViewer implements ResultViewer {
    /**
     * Реалізує вивід результатів у текстовому форматі.
     * @param results список результатів для друку
     */
    @Override
    public void printResults(List<CalculationResult> results) {
        System.out.println("\n=== ТЕКСТОВИЙ ЗВІТ ОБЧИСЛЕНЬ ===");
        if (results.isEmpty()) {
            System.out.println("Колекція порожня.");
        } else {
            results.forEach(res -> System.out.println("- " + res));
        }
    }
}

/**
 * Спеціалізований візуалізатор для виводу результатів у вигляді текстової таблиці.
 * Розширює ієрархію класів згідно з завданням.
 */
class TableResultViewer extends TextResultViewer {
    /** Ширина колонок таблиці за замовчуванням. */
    private int columnWidth = 15;

    /**
     * Конструктор за замовчуванням для TableResultViewer.
     */
    public TableResultViewer() {}

    /**
     * Перевантажений конструктор для встановлення користувацької ширини колонок.
     * Демонструє механізм перевантаження (Overloading).
     * @param width ширина колонки таблиці
     */
    public TableResultViewer(int width) {
        this.columnWidth = width;
    }

    /**
     * Виводить результати у вигляді форматованої таблиці.
     * Перевизначає базовий метод (Overriding).
     * @param results список результатів для відображення
     */
    @Override
    public void printResults(List<CalculationResult> results) {
        if (results.isEmpty()) {
            System.out.println("Таблиця порожня.");
            return;
        }
        String line = "-".repeat(columnWidth * 4 + 5);
        String format = "| %-" + (columnWidth-2) + "s ";
        
        System.out.println(line);
        System.out.printf(format + format + format + format + "|\n", "Маса", "Висота", "Енергія", "1-ці (max)");
        System.out.println(line);
        
        for (CalculationResult res : results) {
            System.out.printf("| %-" + (columnWidth-2) + ".2f | %-" + (columnWidth-2) + ".2f | %-" + (columnWidth-2) + ".2f | %-" + (columnWidth-2) + "d |\n",
                    res.mass, res.height, res.energy, res.maxConsecutiveOnes);
        }
        System.out.println(line);
    }
}

/**
 * Інтерфейс для створення об'єктів-візуалізаторів.
 * Реалізує шаблон проектування Factory Method (Creator).
 */
interface ViewerFactory {
    /**
     * Створює екземпляр візуалізатора.
     * @return об'єкт, що реалізує {@link ResultViewer}
     */
    ResultViewer createViewer();
}

/**
 * Фабрика для створення простого текстового візуалізатора.
 */
class TextViewerFactory implements ViewerFactory {
    /**
     * Створює об'єкт {@link TextResultViewer}.
     * @return новий екземпляр текстового візуалізатора
     */
    @Override
    public ResultViewer createViewer() {
        return new TextResultViewer();
    }
}

/**
 * Фабрика для створення табличного візуалізатора.
 * Дозволяє налаштовувати параметри відображення.
 */
class TableViewerFactory implements ViewerFactory {
    /** Ширина колонки майбутньої таблиці. */
    private int width = 15;

    /**
     * Встановлює параметри відображення таблиці.
     * Приклад перевантаження (Overloading).
     * @param width ширина колонки
     */
    public void setParams(int width) {
        this.width = width;
    }

    /**
     * Створює об'єкт {@link TableResultViewer} із заданими параметрами.
     * @return новий екземпляр табличного візуалізатора
     */
    @Override
    public ResultViewer createViewer() {
        return new TableResultViewer(width);
    }
}

/**
 * Клас, що реалізує бізнес-логіку обчислень.
 */
class EnergySolver {
    /** Прискорення вільного падіння (м/с²). */
    private static final double G = 9.81;

    /**
     * Розраховує потенційну енергію та аналізує її двійкове представлення.
     * @param mass маса об'єкта
     * @param height висота підйому
     * @return об'єкт {@link CalculationResult} з результатами
     */
    public CalculationResult solve(double mass, double height) {
        double energyDouble = mass * G * height;
        long energyInt = (long) energyDouble;
        
        String binary = Long.toBinaryString(energyInt);
        int maxOnes = 0, currentOnes = 0;

        for (char c : binary.toCharArray()) {
            if (c == '1') {
                currentOnes++;
                if (currentOnes > maxOnes) maxOnes = currentOnes;
            } else {
                currentOnes = 0;
            }
        }
        return new CalculationResult(mass, height, energyDouble, maxOnes);
    }
}

/**
 * Клас для проведення автоматичного тестування функціональності.
 */
class FunctionalityTester {
    /**
     * Виконує перевірку коректності обчислень та логіки аналізу бітів.
     * @param solver об'єкт для виконання розрахунків
     */
    public static void test(EnergySolver solver) {
        System.out.println("\n[ТЕСТ] Перевірка розрахунку (m=10.2, h=10)...");
        CalculationResult r = solver.solve(10.2, 10);
        if (r.energy > 990 && r.maxConsecutiveOnes >= 1) {
            System.out.println("[ТЕСТ ПРОЙДЕНО]");
        } else {
            System.out.println("[ТЕСТ НЕ ВДАВСЯ]");
        }
    }
}

/**
 * Основний клас програми, що реалізує діалоговий інтерфейс користувача.
 * Координує роботу фабрик, логіки та збереження даних.
 */
public class MainApp {
    /** Ім'я файлу для серіалізації даних. */
    private static final String FILE_NAME = "collection_data.ser";
    /** Колекція результатів обчислень. */
    private static List<CalculationResult> collection = new ArrayList<>();
    /** Поточна фабрика візуалізації. */
    private static ViewerFactory currentFactory = new TextViewerFactory();

    /**
     * Конструктор за замовчуванням для класу MainApp.
     */
    public MainApp() {
    
    }

    /**
     * Точка входу в програму. Забезпечує роботу головного циклу меню.
     * @param args масив аргументів командного рядка
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EnergySolver solver = new EnergySolver();

        while (true) {
            System.out.println("\n--- МЕНЮ ПРОГРАМИ ---");
            System.out.println("1. Розрахувати та додати");
            System.out.println("2. Вивести списком (Factory Method)");
            System.out.println("3. Вивести таблицею (User Params)");
            System.out.println("4. Зберегти/Відновити дані");
            System.out.println("5. Автоматичний тест");
            System.out.println("0. Вихід");
            System.out.print("Вибір: ");

            int choice = scanner.hasNextInt() ? scanner.nextInt() : -1;
            if (choice == -1) { scanner.next(); continue; }

            switch (choice) {
                case 1 -> {
                    System.out.print("Маса (кг): "); double m = scanner.nextDouble();
                    System.out.print("Висота (м): "); double h = scanner.nextDouble();
                    collection.add(solver.solve(m, h));
                }
                case 2 -> {
                    currentFactory = new TextViewerFactory();
                    display(currentFactory);
                }
                case 3 -> {
                    System.out.print("Ширина колонок таблиці: ");
                    int w = scanner.nextInt();
                    TableViewerFactory tf = new TableViewerFactory();
                    tf.setParams(w);
                    currentFactory = tf;
                    display(currentFactory);
                }
                case 4 -> storageMenu(scanner);
                case 5 -> FunctionalityTester.test(solver);
                case 0 -> {
                    System.out.println("Завершення...");
                    return;
                }
            }
        }
    }

    /**
     * Демонструє поліморфізм та динамічне призначення методів.
     * @param factory абстрактна фабрика для створення візуалізатора
     */
    private static void display(ViewerFactory factory) {
        ResultViewer viewer = factory.createViewer();
        viewer.printResults(collection);
    }

    /**
     * Меню керування сховищем даних.
     * @param sc об'єкт Scanner для вводу
     */
    private static void storageMenu(Scanner sc) {
        System.out.println("1. Зберегти 2. Завантажити");
        if (sc.hasNextInt()) {
            int sub = sc.nextInt();
            if (sub == 1) save(); else load();
        }
    }

    /**
     * Зберігає колекцію результатів у файл.
     */
    private static void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(collection);
            System.out.println("Дані збережено.");
        } catch (IOException e) { System.out.println("Помилка збереження."); }
    }

    /**
     * Відновлює колекцію результатів з файлу.
     */
    @SuppressWarnings("unchecked")
    private static void load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            collection = (List<CalculationResult>) ois.readObject();
            System.out.println("Дані відновлено.");
        } catch (Exception e) { System.out.println("Помилка завантаження."); }
    }
}