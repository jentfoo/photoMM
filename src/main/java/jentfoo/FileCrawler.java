package jentfoo;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileCrawler {
  public static Stream<File> crawl(File root, Predicate<File> fileFilter) {
    if (root.isDirectory()) {
      File[] rootList = root.listFiles();
      Arrays.sort(rootList, (f1, f2) -> f1.getName().compareTo(f2.getName()));
      return Arrays.asList(rootList).stream().flatMap(f -> crawl(f, fileFilter));
    } else if (fileFilter.test(root)) {
      return Collections.singleton(root).stream();
    } else {
      return Collections.<File>emptyList().stream();
    }
  }
}
