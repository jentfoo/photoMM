package jentfoo;

import java.io.File;

import org.threadly.util.Pair;

public class AnalyzerConfig {
  public static final boolean ANALYZE_ONLY_EDITED = false;
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final Pair<Integer, Integer>[] LENS_FOCAL_LENGTHS = 
      new Pair[] { new Pair<>(18, 135), new Pair<>(55, 250), 
                   new Pair<>(35, 150), new Pair<>(70, 200) };
  
  public static boolean isOriginalImageFile(File f) {
    String name = f.getName();
    return name.startsWith("IMG") && name.endsWith(".JPG") && ! name.contains("edit");
  }
  
  public static boolean isEditedImageFile(File f) {
    String name = f.getName();
    return name.startsWith("IMG") && name.contains("edit") && 
              (name.endsWith(".jpg") || name.endsWith(".JPG"));
  }
}
