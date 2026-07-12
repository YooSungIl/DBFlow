package io.dbflow.common.console;

import io.dbflow.common.Exception.ValidationException;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class PromptHelper {
    private final Scanner scanner;

    public PromptHelper() {
        this(System.in);
    }

    public PromptHelper(InputStream inputStream) {
        this.scanner = new Scanner(inputStream);
    }

    public String input(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    public String editInput(String label, String defaultValue) {
        System.out.print(label + " [" + defaultValue + "] : ");
        return scanner.nextLine().trim();
    }

    public String inputRequired(String label, Consumer<String> validator) {
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

    public Integer inputRequiredInt(String label, Consumer<Integer> validator) {
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

    public String inputSelect(String title, List<String> options) {
        System.out.println(title);

        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, options.get(i));
        }

        while (true) {
            System.out.print("선택: ");
            String input = scanner.nextLine().trim();

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

    public String inputEdit(String label, Consumer<String> validator, String defaultValue) {
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

    public Integer inputEditInt(String label, Consumer<Integer> validator, Integer defaultValue) {
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
    public String inputMultiLine(String title) {
        System.out.println();
        System.out.println(title + "(빈 줄을 두 번 입력하면 종료됩니다.)");
        StringBuilder sb = new StringBuilder();
        boolean previousBlank = false;
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
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
