package com.codeborne.selenide;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DummySwitchesTest {
  private final DummySwitches switches = new DummySwitches();

  @Test
  void calculates_hypotenuse() {
    assertThat(switches.calculateHypotenuse(0, 0)).isEqualTo(0);
    assertThat(switches.calculateHypotenuse(0, 2)).isEqualTo(2);
    assertThat(switches.calculateHypotenuse(3, 0)).isEqualTo(3);
    assertThat(switches.calculateHypotenuse(3, 4)).isEqualTo(5);
  }
}
