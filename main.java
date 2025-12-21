import java.util.*;

// Product class to represent inventory item
class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;

    public Product(String id, String name, double price, int quantity) {
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Price and quantity must be non-negative");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    // Setters with validation
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    // Domain-level stock operations
    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increase amount must be positive");
        }
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be positive");
        }
        if (amount > quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        this.quantity -= amount;
    }

    @Override
    public String toString() {
        return String.format("| %-8s | %-20s | %-10.2f | %-8d |",
                id, name, price, quantity);
    }
}

// Custom exception for inventory operations
class InventoryException extends Exception {
    public InventoryException(String message) {
        super(message);
    }
}

// Main Inventory Management System
public class InventoryManagementSystem {
    private static final int LOW_STOCK_LIMIT = 5;

    private Map<String, Product> inventory;
    private Scanner scanner;

    public InventoryManagementSystem() {
        inventory = new HashMap<>();
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new InventoryManagementSystem().run();
    }

    public void run() {
        int choice;
        System.out.println("=== INVENTORY MANAGEMENT SYSTEM ===");

        do {
            showMenu();
            choice = getIntInput("Enter your choice: ");

            try {
                switch (choice) {
                    case 1 -> addProduct();
                    case 2 -> updateProduct();
                    case 3 -> deleteProduct();
                    case 4 -> viewInventory();
                    case 5 -> searchProduct();
                    case 6 -> issueStock();
                    case 7 -> restock();
                    case 0 -> System.out.println("Thank you for using the system!");
                    default -> System.out.println("Invalid choice!");
                }
            } catch (InventoryException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

            if (choice != 0) pressEnterToContinue();
        } while (choice != 0);

        scanner.close();
    }

    private void showMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("1. Add Product");
        System.out.println("2. Update Product");
        System.out.println("3. Delete Product");
        System.out.println("4. View Inventory");
        System.out.println("5. Search Product");
        System.out.println("6. Issue Stock");
        System.out.println("7. Restock");
        System.out.println("0. Exit");
        System.out.println("=".repeat(50));
    }

    private void addProduct() throws InventoryException {
        String id = getStringInput("Enter Product ID: ").toUpperCase();
        if (inventory.containsKey(id)) {
            throw new InventoryException("Product already exists!");
        }

        String name = getStringInput("Enter Name: ");
        double price = getDoubleInput("Enter Price: ");
        int qty = getIntInput("Enter Quantity: ");

        inventory.put(id, new Product(id, name, price, qty));
        System.out.println("✓ Product added successfully");
    }

    private void updateProduct() throws InventoryException {
        String id = getStringInput("Enter Product ID: ").toUpperCase();
        Product p = inventory.get(id);

        if (p == null) throw new InventoryException("Product not found");

        System.out.println("Current: " + p);

        if (getYesNo("Update name?")) {
            p.setName(getStringInput("Enter new name: "));
        }
        if (getYesNo("Update price?")) {
            p.setPrice(getDoubleInput("Enter new price: "));
        }
        if (getYesNo("Update quantity?")) {
            int newQty = getIntInput("Enter new quantity: ");
            if (newQty < 0) throw new InventoryException("Quantity cannot be negative");
            // direct assignment allowed here for full overwrite
            p.decreaseQuantity(p.getQuantity());
            p.increaseQuantity(newQty);
        }

        System.out.println("✓ Product updated");
    }

    private void deleteProduct() throws InventoryException {
        String id = getStringInput("Enter Product ID: ").toUpperCase();
        if (inventory.remove(id) == null) {
            throw new InventoryException("Product not found");
        }
        System.out.println("✓ Product deleted");
    }

    private void viewInventory() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.printf("%-8s | %-20s | %-10s | %-8s |%n",
                "ID", "NAME", "PRICE", "QTY");
        System.out.println("-".repeat(60));

        double totalValue = 0;

        for (Product p : inventory.values()) {
            System.out.print(p);
            if (p.getQuantity() < LOW_STOCK_LIMIT) {
                System.out.print(" ⚠ LOW STOCK");
            }
            System.out.println();
            totalValue += p.getPrice() * p.getQuantity();
        }

        System.out.println("-".repeat(60));
        System.out.printf("Total Inventory Value: %.2f%n", totalValue);
        System.out.println("=".repeat(60));
    }

    private void searchProduct() {
        String key = getStringInput("Enter ID or name: ").toLowerCase();
        boolean found = false;

        for (Product p : inventory.values()) {
            if (p.getId().toLowerCase().contains(key) ||
                p.getName().toLowerCase().contains(key)) {
                System.out.println(p);
                found = true;
            }
        }

        if (!found) System.out.println("No product found");
    }

    private void issueStock() throws InventoryException {
        String id = getStringInput("Enter Product ID: ").toUpperCase();
        Product p = inventory.get(id);

        if (p == null) throw new InventoryException("Product not found");

        int qty = getIntInput("Enter quantity to issue: ");
        p.decreaseQuantity(qty);
        System.out.println("✓ Stock issued");
    }

    private void restock() throws InventoryException {
        String id = getStringInput("Enter Product ID: ").toUpperCase();
        Product p = inventory.get(id);

        if (p == null) throw new InventoryException("Product not found");

        int qty = getIntInput("Enter quantity to add: ");
        p.increaseQuantity(qty);
        System.out.println("✓ Product restocked");
    }

    // Helper methods
    private String getStringInput(String msg) {
        System.out.print(msg);
        return scanner.nextLine().trim();
    }

    private int getIntInput(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid integer");
            }
        }
    }

    private double getDoubleInput(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number");
            }
        }
    }

    private boolean getYesNo(String msg) {
        System.out.print(msg + " (y/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    private void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
