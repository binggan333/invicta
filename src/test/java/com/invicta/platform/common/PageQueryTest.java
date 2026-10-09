package com.invicta.platform.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryTest {
    @Test
    void calculatesOffsetUsingZeroBasedPages() {
        assertThat(new PageQuery(3, 25).offset()).isEqualTo(75);
    }

    @Test
    void rejectsUnboundedPageSizes() {
        assertThatThrownBy(() -> new PageQuery(0, 201)).isInstanceOf(IllegalArgumentException.class);
    }
}
