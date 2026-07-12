package io.dbflow.common.console;

import io.dbflow.common.validation.CommonValidation;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromptHelperTest {

    @Test
    void 필수값이_유효할_때까지_입력을_반복한다() {
        PromptHelper promptHelper = promptHelper("\nvalue\n");

        String result = promptHelper.inputRequired("필수값", CommonValidation::required);

        assertEquals("value", result);
    }

    @Test
    void 유효한_숫자가_입력될_때까지_반복한다() {
        PromptHelper promptHelper = promptHelper("text\n0\n5432\n");

        Integer result = promptHelper.inputRequiredInt("Port", CommonValidation::validatePort);

        assertEquals(5432, result);
    }

    @Test
    void 선택지에_포함된_번호가_입력될_때까지_반복한다() {
        PromptHelper promptHelper = promptHelper("text\n3\n2\n");

        String result = promptHelper.inputSelect("DB 종류", List.of("POSTGRESQL", "MYSQL"));

        assertEquals("MYSQL", result);
    }

    @Test
    void 편집값이_비어_있으면_기본값을_반환한다() {
        PromptHelper promptHelper = promptHelper("\n");

        String result = promptHelper.inputEdit("Host", CommonValidation::required, "localhost");

        assertEquals("localhost", result);
    }

    @Test
    void Console이_없는_환경에서도_필수_비밀번호를_입력받는다() {
        PromptHelper promptHelper = promptHelper("\nsecret-password\n");

        String result = promptHelper.inputRequiredPassword("Password", CommonValidation::required);

        assertEquals("secret-password", result);
    }

    @Test
    void 비밀번호_편집값이_비어_있으면_마스킹_기본값을_반환한다() {
        PromptHelper promptHelper = promptHelper("\n");

        String result = promptHelper.inputEditPassword("Password", CommonValidation::required, "********");

        assertEquals("********", result);
    }

    @Test
    void 빈_줄이_두_번_입력되면_여러_줄_입력을_종료한다() {
        PromptHelper promptHelper = promptHelper("첫 번째 줄\n두 번째 줄\n\n\n");

        String result = promptHelper.inputMultiLine("설명");

        assertEquals("첫 번째 줄" + System.lineSeparator() + "두 번째 줄", result);
    }

    private PromptHelper promptHelper(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        return new PromptHelper(inputStream);
    }
}
