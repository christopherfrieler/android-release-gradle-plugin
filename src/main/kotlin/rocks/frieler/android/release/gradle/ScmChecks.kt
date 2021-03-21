package rocks.frieler.android.release.gradle

internal fun GitRepository.assertMasterBranch() {
    val gitBranch = getCurrentBranch()
    check(gitBranch == "master") { "not on branch 'master'! current branch is '$gitBranch'." }
}

internal fun GitRepository.assertNoLocalChanges() {
    check(!hasLocalChanges()) { "there are uncommitted changes!" }
}
