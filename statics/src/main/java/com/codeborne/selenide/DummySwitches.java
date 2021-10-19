package com.codeborne.selenide;

public class DummySwitches {
  public int calculateHypotenuse(final int legA, final int legB) {
    return switch (legA) {
      case 0 -> legB;
      default -> switch (legB) {
        case 0 -> legA;
        default -> (int) Math.sqrt(legA * legA + legB * legB);
      };
    };
  }
}
