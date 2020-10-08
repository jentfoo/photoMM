# photoMM
Tool to analyze a number of jpg's and analyze the EXIF data to determine what MM the photo was shot with.  It then generalizes what lens would have accommodated most photos.

This project is focused on some personal goals of mine, not currently very generalized.  If this is useful for other people, please file an issue and I can put some effort into it.

## Instructions
1) Compile: `./gradlew build`
2) Execute: `java -cp build/libs/photoMM-0.1.jar jentfoo.PhotoAnalyzer /path/to/photos`

This is designed to work with Canon's filename format.  It can detect edited photos using the format `IMG_XXXX-edit.jpg` vs originals using the format `IMG_XXXX.JPG`.  Adjusting these options should be centralized to `AnalyzerConfig.java`.
