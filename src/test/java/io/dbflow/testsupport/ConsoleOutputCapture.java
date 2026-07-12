package io.dbflow.testsupport;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ConsoleOutputCapture implements AutoCloseable {

    private final PrintStream originalStandardOutput;
    private final PrintStream originalErrorOutput;
    private final ByteArrayOutputStream standardOutputBuffer;
    private final ByteArrayOutputStream errorOutputBuffer;
    private final PrintStream capturedStandardOutput;
    private final PrintStream capturedErrorOutput;
    private boolean closed;

    public ConsoleOutputCapture() {
        originalStandardOutput = System.out;
        originalErrorOutput = System.err;
        standardOutputBuffer = new ByteArrayOutputStream();
        errorOutputBuffer = new ByteArrayOutputStream();
        capturedStandardOutput = new PrintStream(standardOutputBuffer, true, StandardCharsets.UTF_8);
        capturedErrorOutput = new PrintStream(errorOutputBuffer, true, StandardCharsets.UTF_8);

        System.setOut(capturedStandardOutput);
        System.setErr(capturedErrorOutput);
    }

    public String standardOutput() {
        capturedStandardOutput.flush();
        return standardOutputBuffer.toString(StandardCharsets.UTF_8);
    }

    public String errorOutput() {
        capturedErrorOutput.flush();
        return errorOutputBuffer.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        System.setOut(originalStandardOutput);
        System.setErr(originalErrorOutput);
        capturedStandardOutput.close();
        capturedErrorOutput.close();
        closed = true;
    }
}
