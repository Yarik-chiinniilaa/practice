import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Клас для зберігання параметрів і результатів обчислень.
 * Реалізує інтерфейс {@link Serializable} для збереження стану.
 * @author Студент
 * @version 1.0
 */
class CalculationResult implements Serializable {
    /** Унікальний ідентифікатор версії серіалізації */
    private static final long serialVersionUID = 1L;
    
    /** Маса тіла в кг */
    public double mass;
    /** Висота підйому в метрах */
    public double height;
    /** Потенційна енергія в Джоулях */
    public double energy;
    /** Максимальна кількість послідовних одиниць у бінарному представленні */
    public int maxConsecutiveOnes;

    /**
     * Конструктор класу результатів.
     * @param mass маса тіла
     * @param height висота підйому
     * @param energy обчислена енергія
     * @param maxOnes результат бітового аналізу
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
 * Інтерфейс "фабрикованих" об'єктів для відображення результатів.
 * Визначає методи для виводу даних користувачеві.
 */
interface ResultViewer {
    /** * Виводить результати у консоль.
     * @param results список об'єктів для відображення 
     */
    void printResults(List<CalculationResult> results);
}

/**
 * Реалізація методів виведення у текстовому вигляді.
 * Виводить дані у форматі простого списку.
 */
class TextResultViewer implements ResultViewer {
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
 * Інтерфейс для "фабрикуючого" методу.
 * Використовується для створення об'єктів типу ResultViewer.
 */
interface ViewerFactory {
    /** @return екземпляр конкретного візуалізатора */
    ResultViewer createViewer();
}

/**
 * Конкретна фабрика для текстових об'єктів.
 * Повертає екземпляр класу TextResultViewer.
 */
class TextViewerFactory implements ViewerFactory {
    @Override
    public ResultViewer createViewer() {
        return new TextResultViewer();
    }
}

/**
 * Клас для знаходження рішення задачі. 
 * Виконує обчислення та логічний аналіз даних.
 */
class EnergySolver {
    /** Прискорення вільного падіння */
    private static final double G = 9.81;

    /**
     * Виконує розрахунок енергії та визначає найбільшу кількість послідовних 1 у двійковому коді.
     * @param mass маса тіла
     * @param height висота підйому
     * @return новий об'єкт CalculationResult з результатами
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
 * Головний клас додатка для демонстрації діалогового режиму.
 * Координує роботу всіх компонентів системи.
 * @author Студент
 * @version 1.0
 */
public class MainApp {
    /** Шлях до файлу збереження колекції */
    private static final String FILE_NAME = "collection_data.ser";
    /** Список для зберігання результатів (Завдання 1: Колекція) */
    private static List<CalculationResult> collection = new ArrayList<>();
    /** Тимчасовий статус сесії */
    private static transient String sessionStatus = "Активна";

    /**
     * Конструктор за замовчуванням для MainApp.
     */
    public MainApp() {
        // Конструктор ініціалізації
    }

    /**
     * Точка входу в програму. Забезпечує роботу інтерфейсу користувача.
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EnergySolver solver = new EnergySolver();
        
        // Використання Factory Method
        ViewerFactory factory = new TextViewerFactory();
        ResultViewer viewer = factory.createViewer();

        while (true) {
            System.out.println("\n--- МЕНЮ (Статус: " + sessionStatus + ") ---");
            System.out.println("1. Розрахувати та додати в колекцію");
            System.out.println("2. Показати всю колекцію (Factory Method)");
            System.out.println("3. Зберегти колекцію (Серіалізація)");
            System.out.println("4. Відновити колекцію (Десеріалізація)");
            System.out.println("5. Автоматичний тест");
            System.out.println("0. Вихід");
            System.out.print("Ваш вибір: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Будь ласка, введіть число.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> {
                    System.out.print("Введіть масу (кг): "); double m = scanner.nextDouble();
                    System.out.print("Введіть висоту (м): "); double h = scanner.nextDouble();
                    collection.add(solver.solve(m, h));
                    System.out.println("Результат додано.");
                }
                case 2 -> viewer.printResults(collection);
                case 3 -> save();
                case 4 -> load();
                case 5 -> runTest(solver);
                case 0 -> {
                    System.out.println("Вихід...");
                    return;
                }
                default -> System.out.println("Невірний вибір.");
            }
        }
    }

    /** Зберігає всю колекцію у файл за допомогою серіалізації. */
    private static void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(collection);
            System.out.println("Колекцію збережено успішно.");
        } catch (IOException e) {
            System.err.println("Помилка збереження: " + e.getMessage());
        }
    }

    /** Відновлює колекцію з файлу за допомогою десеріалізації. */
    @SuppressWarnings("unchecked")
    private static void load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            collection = (List<CalculationResult>) ois.readObject();
            System.out.println("Колекцію відновлено. Записів: " + collection.size());
        } catch (Exception e) {
            System.out.println("Помилка завантаження: файл відсутній або пошкоджений.");
        }
    }

    /** * Виконує автоматичне тестування логіки обчислень. 
     * @param solver екземпляр розв'язувача для тестування
     */
    private static void runTest(EnergySolver solver) {
        System.out.println("Запуск тесту: Енергія 7 (бінарно 111), очікуємо >= 2 одиниці...");
        CalculationResult res = solver.solve(0.714, 1.0); 
        if (res.maxConsecutiveOnes >= 2) { 
            System.out.println("[ТЕСТ ПРОЙДЕНО]");
        } else {
            System.out.println("[ТЕСТ НЕ ПРОЙДЕНО]");
        }
    }
}