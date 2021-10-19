package com.codeborne.selenide;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record DummyRecord(String foo, int bar) {
}
