package io.dbflow.common.console;

import io.dbflow.common.Exception.ValidationException;

import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class PromptHelper {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static String input(String label) {
        System.out.print(label + ": ");
        return SCANNER.nextLine().trim();
    }

    public static String editInput(String label, String defaultValue) {
        System.out.print(label + " [" + defaultValue + "] : ");
        return SCANNER.nextLine().trim();
    }

    public static String inputRequired(String label, Consumer<String> validator) {
        while (true) {
            try {
                String value = input(label);
                validator.accept(value);
                return value;
            } catch (ValidationException e) {
                ConsoleHelper.error(e.getMessage());
            }
        }
    }

    public static Integer inputRequiredInt(String label, Consumer<Integer> validator) {
        while (true) {
            try {
                String input = input(label);
                Integer value = Integer.parseInt(input);
                validator.accept(value);
                return value;
            } catch (NumberFormatException e) {
                ConsoleHelper.error(label + "는 숫자만 입력 가능합니다.");
            } catch (ValidationException e) {
                ConsoleHelper.error(e.getMessage());
            }
        }
    }

    public static String inputSelect(String title, List<String> options) {
        System.out.println(title);

        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, options.get(i));
        }

        while (true) {
            System.out.print("선택: ");
            String input = SCANNER.nextLine().trim();

            try {
                int index = Integer.parseInt(input) - 1;

                if (index >= 0 && index < options.size()) {
                    return options.get(index);
                }
                ConsoleHelper.error("선택지에 없는 숫자입니다.");
            } catch (NumberFormatException ignored) {
                ConsoleHelper.error("숫자를 입력해 주세요.");
            }
        }
    }

    public static String inputEdit(String label, Consumer<String> validator, String defaultValue) {
        while (true) {
            try {
                String value = editInput(label, defaultValue);

                if (value == null || value.isBlank()) {
                    return defaultValue;
                }

                validator.accept(value);
                return value;
            } catch (ValidationException e) {
                ConsoleHelper.error(e.getMessage());
            }
        }
    }

    public static Integer inputEditInt(String label, Consumer<Integer> validator, Integer defaultValue) {
        while (true) {
            try {
                String input = editInput(label, defaultValue.toString());
                if (input == null || input.isBlank()) {
                    return defaultValue;
                }
                Integer value = Integer.parseInt(input);
                validator.accept(value);
                return value;
            } catch (NumberFormatException e) {
                ConsoleHelper.error(label + "는 숫자만 입력 가능합니다.");
            } catch (ValidationException e) {
                ConsoleHelper.error(e.getMessage());
            }
        }
    }

    /**
     * 여러 줄 입력
     * 빈 줄을 두 번 입력하면 종료
     */
    public static String inputMultiLine(String title) {
        System.out.println();
        System.out.println(title + "(빈 줄을 두 번 입력하면 종료됩니다.)");
        StringBuilder sb = new StringBuilder();
        boolean previousBlank = false;
        while (true) {
            System.out.print("> ");
            String line = SCANNER.nextLine();
            if (line.isBlank()) {
                if (previousBlank) {
                    break;
                }
                previousBlank = true;
                sb.append(System.lineSeparator());
                continue;
            }
            previousBlank = false;
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }
}
