/**
 * using in main/home screen
 */
package com.util.appupdate

object AppUpdateManager {
    var checkUpdate:Int =0
    var updateContent:String = ""
    var isShowAppUpdate:Boolean = true


    /**
     * return 0: no update
     * return 1: should update
     * return 2: must update
     */
    fun checkUpdate(currentVersion: String, updateInfo: RemoteConfigVersionUpdateModel): Int{

        //seting update content
        updateContent = updateInfo.lastest_update_content

        //check update
        var lastestVersion:String = updateInfo.lastest_version
        if ((currentVersion.isEmpty()) || (lastestVersion.isEmpty()))
            return 0;

        var lCurrentVersion:Long = currentVersion.replace(".","").toLong()
        var lLastestVersion:Long = updateInfo.lastest_version.replace(".","").toLong()
        if ((lCurrentVersion<=1) || (lLastestVersion<=1))
            return 0;

        if (lCurrentVersion >= lLastestVersion)
            return 0;

        //no update
        if (updateInfo.no_update.contains(currentVersion))
            return 0

        //should update
        if (updateInfo.should_update.contains(currentVersion))
            return 1

        //must update
        if (updateInfo.must_update.contains(currentVersion))
            return 2

        //if not config
        return 0

    }
}