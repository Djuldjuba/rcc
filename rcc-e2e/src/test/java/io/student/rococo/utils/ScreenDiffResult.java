package io.student.rococo.utils;

import io.student.rococo.jupiter.extensions.ScreenShotTestExtension;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;

public class ScreenDiffResult implements BooleanSupplier {

  private final BufferedImage expected;
  private final BufferedImage actual;
  private final ImageDiff diff;
  private final boolean hasDif;

  private static final double DIFF_PERCENT_TRIGGER = 2.0;

  public ScreenDiffResult(BufferedImage actual, BufferedImage expected) {
    this.actual = actual;
    this.expected = expected;

    int totalPixels = expected.getWidth() * expected.getHeight();
    int trigger = Math.max(100, (int) (totalPixels * DIFF_PERCENT_TRIGGER / 100.0));

    this.diff = new ImageDiffer()
            .makeDiff(expected, actual)
            .withDiffSizeTrigger(trigger);

    this.hasDif = diff.hasDiff();
  }

  @Override
  public boolean getAsBoolean() {
    if (hasDif) {
      ScreenShotTestExtension.setExpected(expected);
      ScreenShotTestExtension.setActual(actual);
      ScreenShotTestExtension.setDiff(diff.getMarkedImage());
    }
    return hasDif;
  }
}