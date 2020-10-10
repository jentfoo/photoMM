package jentfoo;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileCrawler {
  public static Stream<File> crawl(File[] roots, Predicate<File> fileFilter) {
    return Stream.of(roots).flatMap(f -> crawl(f, fileFilter));
  }
  
  public static Stream<File> crawl(File root, Predicate<File> fileFilter) {
    if (root.isDirectory()) {
      File[] rootList = root.listFiles();
      Arrays.sort(rootList, (f1, f2) -> f1.getName().compareTo(f2.getName()));
      return Stream.of(rootList).flatMap(f -> crawl(f, fileFilter));
    } else if (fileFilter.test(root)) {
      return Stream.of(root);
    } else {
      return Stream.empty();
    }
  }
}
