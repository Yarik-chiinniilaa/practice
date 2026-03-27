import java.io.*;
import java.util.*;

/**
 * Клас для зберігання параметрів і результатів обчислень потенційної енергії.
 * Реалізує {@link Serializable} для збереження стану об'єктів.
 * * @author Студент
 * @version 1.5
 */
class CalculationResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Маса тіла в кг */
    public double mass;
    /** Висота підйому в метрах */
    public double height;
    /** Потенційна енергія в Джоулях */
    public double energy;
    /** Максимальна кількість послідовних одиниць у бінарному представленні цілої частини енергії */
    public int maxConsecutiveOnes;

    /**
     * Конструктор для створення об'єкта з результатами обчислень.
     * * @param mass маса тіла
     * @param height висота підйому
     * @param energy розрахована енергія
     * @param maxOnes макс. кількість одиниць підряд у двійковому коді
     */
    public CalculationResult(double mass, double height, double energy, int maxOnes) {
        this.mass = mass;
        this.height = height;
        this.energy = energy;
        this.maxConsecutiveOnes = maxOnes;
    }

    @Override
    public String toString() {
        return String.format("Параметри: [m=%.2f, h=%.2f] | Енергія: %.2f | Макс. посл. 1-ць: %d", 
                mass, height, energy, maxConsecutiveOnes);
    }
}

/**
 * Клас для розрахунку енергії та аналізу бінарного представлення результату.
 * Реалізовано за шаблоном Singleton.
 */
class EnergySolver {
    private static EnergySolver instance;
    private static final double G = 9.81;

    /** Приватний конструктор для запобігання створенню екземплярів ззовні. */
    private EnergySolver() {}

    /**
     * Повертає єдиний екземпляр класу {@link EnergySolver}.
     * @return об'єкт EnergySolver
     */
    public static EnergySolver getInstance() {
        if (instance == null) {
            instance = new EnergySolver();
        }
        return instance;
    }

    /**
     * Розраховує потенційну енергію та знаходить найдовшу послідовність одиниць
     * у її двійковому представленні.
     * * @param mass маса тіла (кг)
     * @param height висота (м)
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
        System.out.println("Двійкове подання енергії (" + energyInt + "): " + binary);
        return new CalculationResult(mass, height, energyDouble, maxOnes);
    }
}

/**
 * Інтерфейс для реалізації команд (шаблон Command).
 */
interface Command {
    /** Виконання команди. */
    void execute();
    /** Скасування результату виконання команди. */
    void undo();
}

/**
 * Команда для додавання результату обчислення до списку.
 */
class AddCommand implements Command {
    private List<CalculationResult> list;
    private CalculationResult result;

    /**
     * @param list список, куди додається результат
     * @param result об'єкт результату
     */
    public AddCommand(List<CalculationResult> list, CalculationResult result) {
        this.list = list;
        this.result = result;
    }

    @Override public void execute() { list.add(result); }
    @Override public void undo() { list.remove(result); }
}

/**
 * Команда, що дозволяє групувати декілька команд в одну транзакцію (Macro).
 */
class MacroCommand implements Command {
    private List<Command> commands = new ArrayList<>();
    
    /**
     * Додає команду до списку макрокоманди.
     * @param cmd об'єкт команди
     */
    public void addCommand(Command cmd) { commands.add(cmd); }

    @Override public void execute() { commands.forEach(Command::execute); }
    @Override public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) commands.get(i).undo();
    }
}

/**
 * Інтерфейс для візуалізації результатів обчислень.
 */
interface ResultViewer { 
    /**
     * Виводить список результатів у консоль.
     * @param results список об'єктів для відображення
     */
    void printResults(List<CalculationResult> results); 
}

/**
 * Реалізація візуалізатора у вигляді текстового звіту.
 */
class TextResultViewer implements ResultViewer {
    @Override
    public void printResults(List<CalculationResult> results) {
        System.out.println("\n=== ТЕКСТОВИЙ ЗВІТ ===");
        if (results.isEmpty()) System.out.println("Колекція порожня.");
        else results.forEach(res -> System.out.println("- " + res));
    }
}

/**
 * Реалізація візуалізатора у вигляді таблиці.
 */
class TableResultViewer implements ResultViewer {
    private int width;
    /** @param width ширина колонок */
    public TableResultViewer(int width) { this.width = width; }

    @Override
    public void printResults(List<CalculationResult> results) {
        if (results.isEmpty()) { System.out.println("Таблиця порожня."); return; }
        String line = "-".repeat(width * 4 + 5);
        System.out.println(line);
        System.out.printf("| %-10s | %-10s | %-10s | %-5s |\n", "Маса", "Висота", "Енергія", "1-ці");
        System.out.println(line);
        for (CalculationResult res : results) {
            System.out.printf("| %-10.2f | %-10.2f | %-10.2f | %-5d |\n",
                    res.mass, res.height, res.energy, res.maxConsecutiveOnes);
        }
        System.out.println(line);
    }
}

/**
 * Фабрика для створення об'єктів візуалізації.
 */
interface ViewerFactory { 
    /** @return об'єкт {@link ResultViewer} */
    ResultViewer createViewer(); 
}

/** Фабрика текстового відображення. */
class TextViewerFactory implements ViewerFactory {
    @Override public ResultViewer createViewer() { return new TextResultViewer(); }
}

/** Фабрика табличного відображення. */
class TableViewerFactory implements ViewerFactory {
    private int width = 15;
    /** @param w ширина колонок */
    public void setWidth(int w) { this.width = w; }
    @Override public ResultViewer createViewer() { return new TableResultViewer(width); }
}

/**
 * Головний клас додатка. Містить точку входу та логіку меню.
 */
public class MainApp {
    private static final String FILE_NAME = "collection_data.ser";
    private static List<CalculationResult> collection = new ArrayList<>();
    private static Stack<Command> history = new Stack<>();
    private static transient String sessionStatus = "Активна";

    /**
     * Приватний конструктор для запобігання створенню екземплярів головного класу.
     * Використовується для усунення попереджень Javadoc.
     */
    private MainApp() {
        throw new UnsupportedOperationException("Це утилітарний клас");
    }

    /**
     * Точка входу в програму.
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EnergySolver solver = EnergySolver.getInstance();
        TableViewerFactory tableFactory = new TableViewerFactory();

        while (true) {
            System.out.println("\n--- МЕНЮ (Статус: " + sessionStatus + ") ---");
            System.out.println("1. Розрахувати та додати");
            System.out.println("2. Скасувати останню дію (Undo)");
            System.out.println("3. Виконати макрокоманду");
            System.out.println("4. Показати списком");
            System.out.println("5. Показати таблицею");
            System.out.println("6. Зберегти колекцію");
            System.out.println("7. Відновити колекцію");
            System.out.println("8. Автоматичний тест");
            System.out.println("0. Вихід");
            System.out.print("Вибір: ");

            if (!scanner.hasNextInt()) { scanner.next(); continue; }
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.print("Маса (кг): "); double m = scanner.nextDouble();
                    System.out.print("Висота (м): "); double h = scanner.nextDouble();
                    execute(new AddCommand(collection, solver.solve(m, h)));
                    System.out.println("Результат додано.");
                }
                case 2 -> {
                    if (!history.isEmpty()) history.pop().undo();
                    else System.out.println("Немає дій для скасування.");
                }
                case 3 -> {
                    MacroCommand macro = new MacroCommand();
                    macro.addCommand(new AddCommand(collection, solver.solve(1, 1)));
                    macro.addCommand(new AddCommand(collection, solver.solve(5, 10)));
                    execute(macro);
                    System.out.println("Макрокоманду виконано.");
                }
                case 4 -> new TextViewerFactory().createViewer().printResults(collection);
                case 5 -> {
                    System.out.print("Ширина колонок: ");
                    tableFactory.setWidth(scanner.nextInt());
                    tableFactory.createViewer().printResults(collection);
                }
                case 6 -> save();
                case 7 -> load();
                case 8 -> runTest(solver);
                case 0 -> { System.out.println("Вихід..."); return; }
                default -> System.out.println("Невірний вибір.");
            }
        }
    }

    /**
     * Виконує команду та зберігає її в історії.
     * @param cmd команда
     */
    private static void execute(Command cmd) {
        cmd.execute();
        history.push(cmd);
    }

    /** Зберігає дані у файл. */
    private static void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(collection);
            System.out.println("Дані збережено.");
        } catch (IOException e) { System.err.println("Помилка: " + e.getMessage()); }
    }

    /** Завантажує дані з файлу. */
    @SuppressWarnings("unchecked")
    private static void load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            collection = (List<CalculationResult>) ois.readObject();
            System.out.println("Дані відновлено.");
        } catch (Exception e) { System.out.println("Файл не знайдено."); }
    }

    /** Тест коректності обчислень. */
    private static void runTest(EnergySolver solver) {
        System.out.println("\n[ТЕСТ] Очікуємо >= 2 одиниці...");
        CalculationResult res = solver.solve(0.714, 1.0); 
        if (res.maxConsecutiveOnes >= 2) System.out.println("[ТЕСТ ПРОЙДЕНО]");
        else System.out.println("[ТЕСТ НЕ ПРОЙДЕНО]");
    }
}