Set-ItemProperty -Path target\*.exe -Name IsReadOnly -Value $false
Remove-Item target\*.exe
jpackage  `
--input target\ `
--main-jar sky-launcher-5.0.0-SNAPSHOT.jar `
--main-class info.chrismcgee.sky.artdept.ArtDept `
--type exe `
--dest target\ `
--icon src\main\resources\images\sky_launcher-02.ico `
--win-shortcut `
--win-menu `
--win-menu-group "Sky Unlimited, Inc." `
--name "Sky Launcher" `
--app-version 5.0.0 `
--description "Launcher for the Proofing and Output scripts for Sky Unlimited, Inc.'s Art Department" `
--vendor "Sky Unlimited, Inc." `
--copyright "Copyright ©2021, All rights reserved"
