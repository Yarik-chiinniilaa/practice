import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
 * Інтерфейс для реалізації команд (шаблон Command).
 * Забезпечує підтримку виконання та скасування операцій.
 */
interface Command {
    /** Виконання команди. */
    void execute();
    /** Скасування результату виконання команди. */
    void undo();
}

/**
 * Команда для додавання результату обчислення до списку.
 * Використовує синхронізацію для потокобезпеки.
 */
class AddCommand implements Command {
    private final List<CalculationResult> list;
    private final CalculationResult result;

    /**
     * @param list список, куди додається результат
     * @param result об'єкт результату
     */
    public AddCommand(List<CalculationResult> list, CalculationResult result) {
        this.list = list;
        this.result = result;
    }

    @Override 
    public void execute() { 
        synchronized(list) { list.add(result); } 
    }
    
    @Override 
    public void undo() { 
        synchronized(list) { list.remove(result); } 
    }
}

/**
 * Команда, що дозволяє групувати декілька команд в одну транзакцію (Macro).
 */
class MacroCommand implements Command {
    private final List<Command> commands = new ArrayList<>();
    
    /**
     * Додає команду до списку макрокоманди.
     * @param cmd об'єкт команди
     */
    public void addCommand(Command cmd) { commands.add(cmd); }

    @Override public void execute() { commands.forEach(Command::execute); }
    
    @Override public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }
}

/**
 * Черга команд для виконання в окремому потоці (Worker Thread).
 * Реалізує асинхронну обробку завдань.
 */
class CommandQueue {
    private final BlockingQueue<Command> queue = new LinkedBlockingQueue<>();
    private final Thread worker;

    public CommandQueue() {
        worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Command cmd = queue.take();
                    cmd.execute();
                    System.out.println("\n[Worker] Команду виконано в окремому потоці.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    /** @param cmd команда для додавання в чергу */
    public void put(Command cmd) { queue.add(cmd); }
}

/**
 * Клас для розрахунку енергії та аналізу бінарного представлення результату.
 * Реалізовано за шаблоном Singleton.
 */
class EnergySolver {
    private static EnergySolver instance;
    private static final double G = 9.81;

    private EnergySolver() {}

    /**
     * Повертає єдиний екземпляр класу {@link EnergySolver}.
     * @return об'єкт EnergySolver
     */
    public static EnergySolver getInstance() {
        if (instance == null) instance = new EnergySolver();
        return instance;
    }

    /**
     * Розраховує потенційну енергію та аналізує її двійковий код.
     * * @param mass маса тіла (кг)
     * @param height висота (м)
     * @return об'єкт {@link CalculationResult}
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
 * Інтерфейс для візуалізації результатів (шаблон Strategy).
 */
interface ResultViewer { 
    /** @param results список об'єктів для відображення */
    void printResults(List<CalculationResult> results); 
}

/** Реалізація візуалізатора у вигляді текстового звіту. */
class TextResultViewer implements ResultViewer {
    @Override
    public void printResults(List<CalculationResult> results) {
        System.out.println("\n=== ТЕКСТОВИЙ ЗВІТ ===");
        synchronized(results) {
            if (results.isEmpty()) System.out.println("Колекція порожня.");
            else results.forEach(res -> System.out.println("- " + res));
        }
    }
}

/** Реалізація візуалізатора у вигляді таблиці. */
class TableResultViewer implements ResultViewer {
    private final int width;
    public TableResultViewer(int width) { this.width = width; }

    @Override
    public void printResults(List<CalculationResult> results) {
        synchronized(results) {
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
}

/**
 * Головний клас додатка. Містить логіку меню та управління станом.
 */
public class MainApp {
    private static final String FILE_NAME = "collection_data.ser";
    private static List<CalculationResult> collection = Collections.synchronizedList(new ArrayList<>());
    private static final Stack<Command> history = new Stack<>();
    private static final CommandQueue workerQueue = new CommandQueue();

    private MainApp() { throw new UnsupportedOperationException(); }

    /**
     * Точка входу в програму.
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EnergySolver solver = EnergySolver.getInstance();
        
        while (true) {
            printMenu();
            int choice = scanner.hasNextInt() ? scanner.nextInt() : -1;
            if (choice == -1) { scanner.next(); continue; }

            switch (choice) {
                case 1 -> {
                    System.out.print("Маса: "); double m = scanner.nextDouble();
                    System.out.print("Висота: "); double h = scanner.nextDouble();
                    AddCommand cmd = new AddCommand(collection, solver.solve(m, h));
                    workerQueue.put(cmd);
                    history.push(cmd);
                }
                case 2 -> {
                    if (!history.isEmpty()) history.pop().undo();
                    else System.out.println("Історія порожня.");
                }
                case 3 -> executeMacro(solver);
                case 4 -> new TextResultViewer().printResults(collection);
                case 5 -> new TableResultViewer(15).printResults(collection);
                case 6 -> parallelAnalysis();
                case 7 -> save();
                case 8 -> load();
                case 0 -> System.exit(0);
                default -> System.out.println("Невірний вибір.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- ГОЛОВНЕ МЕНЮ ---");
        System.out.println("1. Розрахувати (Worker Thread)");
        System.out.println("2. Скасувати");
        System.out.println("3. Макрокоманда");
        System.out.println("4. Текстовий звіт");
        System.out.println("5. Табличний звіт");
        System.out.println("6. Паралельна статистика");
        System.out.println("7. Зберегти");
        System.out.println("8. Завантажити");
        System.out.println("0. Вихід");
        System.out.print("Вибір: ");
    }

    private static void executeMacro(EnergySolver solver) {
        MacroCommand macro = new MacroCommand();
        macro.addCommand(new AddCommand(collection, solver.solve(10, 5)));
        macro.addCommand(new AddCommand(collection, solver.solve(20, 2)));
        macro.execute();
        history.push(macro);
        System.out.println("Макрокоманду виконано.");
    }

    /** Паралельна обробка даних за допомогою Stream API. */
    private static void parallelAnalysis() {
        if (collection.isEmpty()) return;
        DoubleSummaryStatistics stats = collection.parallelStream()
                .collect(Collectors.summarizingDouble(r -> r.energy));
        System.out.printf("Середня енергія: %.2f Дж\n", stats.getAverage());
        System.out.printf("Максимум: %.2f Дж\n", stats.getMax());
        System.out.printf("Мінімум: %.2f Дж\n", stats.getMin());
        System.out.printf("Кількість елементів: %d\n", stats.getCount());
    }

    private static void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(new ArrayList<>(collection));
            System.out.println("Збережено.");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private static void load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            List<CalculationResult> loaded = (List<CalculationResult>) ois.readObject();
            collection.clear();
            collection.addAll(loaded);
            System.out.println("Завантажено.");
        } catch (Exception e) { System.out.println("Помилка завантаження."); }
    }
}