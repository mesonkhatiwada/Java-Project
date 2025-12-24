import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class simu {
    static final String RESET = "\u001B[0m";
    static final String RED = "\u001B[31m";
    static final String GREEN = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String BLUE = "\u001B[34m";

    static class Account {
        String user;
        String pin;
        int balance;
        int withdrawToday;
        String lastDate;
        Account(String u, String p, int b, int w, String d) {
            user = u; pin = p; balance = b; withdrawToday = w; lastDate = d;
        }
        String serialize() {
            return user + "|" + pin + "|" + balance + "|" + withdrawToday + "|" + lastDate;
        }
    }

    static List<Account> loadAccounts(String fname) throws Exception {
        List<Account> list = new ArrayList<>();
        File f = new File(fname);
        if (!f.exists()) { f.createNewFile(); return list; }
        Scanner sc = new Scanner(f);
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length < 5) continue;
            String user = parts[0];
            String pin = parts[1];
            int balance = Integer.parseInt(parts[2]);
            int w = Integer.parseInt(parts[3]);
            String d = parts[4];
            list.add(new Account(user, pin, balance, w, d));
        }
        sc.close();
        return list;
    }

    static void saveAccounts(String fname, List<Account> list) throws Exception {
        PrintWriter pw = new PrintWriter(new FileWriter(fname,false));
        for (Account a: list) pw.println(a.serialize());
        pw.close();
    }

    static Account findAccount(List<Account> list, String user) {
        for (Account a: list) if (a.user.equals(user)) return a;
        return null;
    }

    static void appendHistory(String user, String entry) throws Exception {
        String fname = "history_" + user + ".txt";
        String time = java.time.LocalDateTime.now().toString();
        String line = time + " - " + entry + System.lineSeparator();
        File f = new File(fname);
        if (!f.exists()) f.createNewFile();
        Files.write(f.toPath(), line.getBytes(), StandardOpenOption.APPEND);
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String accountsFile = "accounts.txt";
        List<Account> accounts = loadAccounts(accountsFile);
        System.out.println(BLUE + "Welcome to Simple ATM System" + RESET);

        while (true) {
            System.out.print(YELLOW + "Enter username (or type 'exit' to quit): " + RESET);
            String user = sc.next();
            if (user.equalsIgnoreCase("exit")) {
                saveAccounts(accountsFile, accounts);
                System.out.println(GREEN + "Goodbye." + RESET);
                sc.close();
                System.exit(0);
            }
            System.out.print(YELLOW + "Enter PIN: " + RESET);
            String pin = sc.next();
            Account acc = findAccount(accounts, user);
            if (acc == null) { System.out.println(RED + "User not found." + RESET); continue; }
            int attempts = 0;
            while (!acc.pin.equals(pin) && attempts < 2) { attempts++; System.out.println(RED + "Wrong PIN." + RESET); System.out.print(YELLOW + "Enter PIN: " + RESET); pin = sc.next(); }
            if (!acc.pin.equals(pin)) { System.out.println(RED + "Account locked for this session." + RESET); continue; }
            System.out.println(GREEN + "Login successful. Welcome, " + acc.user + "!" + RESET);
            if (acc.user.equals("admin")) { adminMenu(sc, accounts, accountsFile); continue; }
            userMenu(sc, acc, accounts, accountsFile);
        }
    }

    static void adminMenu(Scanner sc, List<Account> accounts, String accountsFile) throws Exception {
        while (true) {
            System.out.println(BLUE + "----- ADMIN MENU -----" + RESET);
            System.out.println("1. View all accounts");
            System.out.println("2. Add new user");
            System.out.println("3. Delete user");
            System.out.println("4. Back to login");
            System.out.print(YELLOW + "Choose: " + RESET);
            int ch = sc.nextInt();
            if (ch == 1) { for (Account a: accounts) System.out.println("User: " + a.user + " | Balance: " + a.balance + " | LastWithdrawDate: " + a.lastDate + " | WithdrawToday: " + a.withdrawToday); }
            else if (ch == 2) {
                System.out.print(YELLOW + "New username: " + RESET); String nu = sc.next();
                if (findAccount(accounts, nu) != null) System.out.println(RED + "User exists." + RESET);
                else { System.out.print(YELLOW + "New PIN: " + RESET); String np = sc.next(); System.out.print(YELLOW + "Starting balance (integer): " + RESET); int b = sc.nextInt(); accounts.add(new Account(nu, np, b, 0, "1970-01-01")); saveAccounts(accountsFile, accounts); System.out.println(GREEN + "User added." + RESET); }
            } else if (ch == 3) { System.out.print(YELLOW + "Username to delete: " + RESET); String du = sc.next(); Account tar = findAccount(accounts, du); if (tar == null) System.out.println(RED + "No such user." + RESET); else if (tar.user.equals("admin")) System.out.println(RED + "Cannot delete admin." + RESET); else { accounts.remove(tar); saveAccounts(accountsFile, accounts); System.out.println(GREEN + "Deleted." + RESET); } }
            else if (ch == 4) break; else System.out.println(RED + "Invalid choice." + RESET);
        }
    }

    static void userMenu(Scanner sc, Account acc, List<Account> accounts, String accountsFile) throws Exception {
        int DAILY_LIMIT = 40000;
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        while (true) {
            LocalDate today = LocalDate.now();
            if (!acc.lastDate.equals(today.format(fmt))) { acc.withdrawToday = 0; acc.lastDate = today.format(fmt); saveAccounts(accountsFile, accounts); }
            System.out.println(BLUE + "----- ATM MENU -----" + RESET);
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transaction History");
            System.out.println("5. Change PIN");
            System.out.println("6. Logout");
            System.out.println("7. Exit Program");
            System.out.print(YELLOW + "Choose: " + RESET);
            int choice = sc.nextInt();
            if (choice == 1) System.out.println(GREEN + "Balance: " + acc.balance + RESET);
            else if (choice == 2) { System.out.print(YELLOW + "Enter deposit amount: " + RESET); int d = sc.nextInt(); if (d<=0) System.out.println(RED + "Invalid amount." + RESET); else { acc.balance += d; saveAccounts(accountsFile, accounts); appendHistory(acc.user, "Deposit: +" + d + " NewBalance: " + acc.balance); System.out.println(GREEN + "Deposited. New balance: " + acc.balance + RESET); } }
            else if (choice == 3) { System.out.print(YELLOW + "Enter withdraw amount: " + RESET); int w = sc.nextInt(); if (w<=0) System.out.println(RED + "Invalid amount." + RESET); else if (w>acc.balance) System.out.println(RED + "Insufficient balance." + RESET); else if (acc.withdrawToday + w > DAILY_LIMIT) System.out.println(RED + "Daily limit exceeded. Remaining daily limit: " + (DAILY_LIMIT - acc.withdrawToday) + RESET); else { acc.balance -= w; acc.withdrawToday += w; saveAccounts(accountsFile, accounts); appendHistory(acc.user, "Withdraw: -" + w + " NewBalance: " + acc.balance); System.out.println(GREEN + "Withdraw successful. Remaining balance: " + acc.balance + RESET); } }
            else if (choice == 4) { String fname = "history_" + acc.user + ".txt"; File hf = new File(fname); if (!hf.exists()) System.out.println(YELLOW + "No history found." + RESET); else { List<String> lines = Files.readAllLines(hf.toPath()); for (String l: lines) System.out.println(l); } }
            else if (choice == 5) { System.out.print(YELLOW + "Enter current PIN: " + RESET); String cur = sc.next(); if (!cur.equals(acc.pin)) System.out.println(RED + "Wrong PIN." + RESET); else { System.out.print(YELLOW + "Enter new PIN: " + RESET); String np = sc.next(); acc.pin = np; saveAccounts(accountsFile, accounts); appendHistory(acc.user, "PIN changed"); System.out.println(GREEN + "PIN changed." + RESET); } }
            else if (choice == 6) { System.out.println(BLUE + "Logging out..." + RESET); break; }
            else if (choice == 7) { saveAccounts(accountsFile, accounts); System.out.println(GREEN + "Goodbye." + RESET); System.exit(0); }
            else System.out.println(RED + "Invalid choice." + RESET);
        }
    }
}