package rocks.frieler.android.release.gradle

fun StringBuilder.appendLineWithSystemEnding(str: String) =
    this.append(str).append(System.lineSeparator())
