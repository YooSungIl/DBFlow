package io.dbflow.testsupport;

import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConsoleOutputCaptureTest {

    @Test
    void 표준_출력과_오류_출력을_각각_캡처한다() {
        try (ConsoleOutputCapture output = new ConsoleOutputCapture()) {
            System.out.print("정상 출력");
            System.err.print("오류 출력");

            assertEquals("정상 출력", output.standardOutput());
            assertEquals("오류 출력", output.errorOutput());
        }
    }

    @Test
    void 종료하면_원래_출력_스트림을_복구한다() {
        PrintStream originalStandardOutput = System.out;
        PrintStream originalErrorOutput = System.err;

        try (ConsoleOutputCapture ignored = new ConsoleOutputCapture()) {
            // try-with-resources 종료 시 원래 스트림으로 복구된다.
        }

        assertSame(originalStandardOutput, System.out);
        assertSame(originalErrorOutput, System.err);
    }
}
