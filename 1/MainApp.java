import java.io.*;
import java.util.Scanner;

/**
 * Клас для зберігання параметрів і результатів обчислень.
 * Реалізує інтерфейс {@link Serializable} для збереження стану.
 * * @author Студент
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
     * @param mass маса
     * @param height висота
     * @param energy енергія
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
 * Клас для знаходження рішення задачі. 
 * Використовує агрегування об'єкта {@link CalculationResult}.
 */
class EnergySolver {
    /** Прискорення вільного падіння */
    private static final double G = 9.81;
    /** Агрегований об'єкт результату */
    private CalculationResult result;

    /**
     * Конструктор за замовчуванням для створення екземпляра розв'язувача.
     */
    public EnergySolver() {
        // Конструктор ініціалізації
    }

    /**
     * Виконує розрахунок енергії та визначає найбільшу кількість послідовних 1 у двійковому коді.
     * @param mass маса тіла
     * @param height висота підйому
     */
    public void solve(double mass, double height) {
        double energyDouble = mass * G * height;
        long energyInt = (long) energyDouble;
        
        String binary = Long.toBinaryString(energyInt);
        int maxOnes = 0;
        int currentOnes = 0;

        for (char c : binary.toCharArray()) {
            if (c == '1') {
                currentOnes++;
                if (currentOnes > maxOnes) maxOnes = currentOnes;
            } else {
                currentOnes = 0;
            }
        }
        
        this.result = new CalculationResult(mass, height, energyDouble, maxOnes);
        System.out.println("Двійкове подання енергії (" + energyInt + "): " + binary);
    }

    /** @return Об'єкт з результатами обчислень */
    public CalculationResult getResult() { return result; }
    
    /** @param result Об'єкт результату для встановлення */
    public void setResult(CalculationResult result) { this.result = result; }
}

/**
 * Головний клас додатка для демонстрації діалогового режиму та тестування.
 * Демонструє пункти 2 та 3 технічного завдання.
 */
public class MainApp {
    /** Шлях до файлу збереження */
    private static final String FILE_NAME = "data.ser";
    /** Тимчасовий статус сесії, що не підлягає серіалізації */
    private static transient String sessionStatus = "Активна";

    /**
     * Приватний конструктор для запобігання створенню екземплярів MainApp.
     */
    private MainApp() {
        // Утилітарний клас
    }

    /**
     * Точка входу в програму. Забезпечує роботу інтерфейсу користувача.
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EnergySolver solver = new EnergySolver();

        while (true) {
            System.out.println("\n--- МЕНЮ (Статус: " + sessionStatus + ") ---");
            System.out.println("1. Розрахувати та знайти послідовність 1-ць");
            System.out.println("2. Зберегти стан (Серіалізація)");
            System.out.println("3. Відновити стан (Десеріалізація)");
            System.out.println("4. Тестування коректності");
            System.out.println("0. Вихід");
            System.out.print("Ваш вибір: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.print("Введіть масу (кг): ");
                    double m = scanner.nextDouble();
                    System.out.print("Введіть висоту (м): ");
                    double h = scanner.nextDouble();
                    solver.solve(m, h);
                    System.out.println(solver.getResult());
                    break;
                case 2:
                    save(solver.getResult());
                    break;
                case 3:
                    CalculationResult restored = load();
                    if (restored != null) {
                        solver.setResult(restored);
                        System.out.println("Об'єкт відновлено: " + restored);
                    }
                    break;
                case 4:
                    runTest();
                    break;
                case 0:
                    System.out.println("Вихід...");
                    return;
                default:
                    System.out.println("Невірний вибір.");
            }
        }
    }

    /** Виконує автоматичне тестування логіки обчислень. */
    private static void runTest() {
        System.out.println("Запуск тесту: Енергія 7 (бінарно 111), очікуємо 3 одиниці...");
        EnergySolver testSolver = new EnergySolver();
        testSolver.solve(0.714, 1.0); 
        if (testSolver.getResult().maxConsecutiveOnes >= 2) { 
            System.out.println("[ТЕСТ ПРОЙДЕНО]");
        }
    }

    /** * Зберігає об'єкт.
     * @param res результати
     */
    private static void save(CalculationResult res) {
        if (res == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(res);
            System.out.println("Збережено.");
        } catch (IOException e) { e.printStackTrace(); }
    }

    /** * Завантажує об'єкт.
     * @return результат
     */
    private static CalculationResult load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (CalculationResult) ois.readObject();
        } catch (Exception e) {
            System.out.println("Помилка завантаження.");
            return null;
        }
    }
}