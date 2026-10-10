package com.invicta.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LocaleConfigTest {
    private final LocaleResolver resolver = new LocaleConfig().localeResolver();

    @Test
    void supportsLanguageQueryParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("lang", "zh-CN");

        assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.SIMPLIFIED_CHINESE);
    }

    @Test
    void supportsAcceptLanguageHeaderAndDefaultsToEnglish() {
        MockHttpServletRequest chinese = new MockHttpServletRequest();
        chinese.addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        MockHttpServletRequest defaultRequest = new MockHttpServletRequest();

        assertThat(resolver.resolveLocale(chinese)).isEqualTo(Locale.SIMPLIFIED_CHINESE);
        assertThat(resolver.resolveLocale(defaultRequest)).isEqualTo(Locale.US);
    }
}
