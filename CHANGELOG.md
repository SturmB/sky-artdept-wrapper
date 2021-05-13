# Changelog

<a name="5.0.0"></a>
## 5.0.0 (2021-05-11)

This update redesigns most of the interface, adding a new coat of paint as well as adding many more settings. Also, the settings have been moved to their own dialog. The app reacts to the color mode that Windows is using (dark vs. light). Finally, the hanging bug that caused the app to still be running in the background even after being closed has (hopefully) been fixed.

### Added

- ✨ Adds Printer Name to arguments for the scripts [[26344b3](https://github.com/skyunlimitedinc/sky-launcher/commit/26344b39004901d9748d0015b2755c7b59f0c20c)]
- 🔊 Fix debug log preference [[c3506d6](https://github.com/skyunlimitedinc/sky-launcher/commit/c3506d68c8e5408e609a4cf18454a968cfbfb5bc)]
- ✨ Adds Patterns file setting [[c88cdfc](https://github.com/skyunlimitedinc/sky-launcher/commit/c88cdfc3ecff7cfe82d6cfcde72747d2a0257023)]
- ✨ Adds Maven [[4384da8](https://github.com/skyunlimitedinc/sky-launcher/commit/4384da8798b71652f522bef1afd29da554f27149)]
- ✨ Adds Settings dialog with a few controls [[5707f0e](https://github.com/skyunlimitedinc/sky-launcher/commit/5707f0e2957d42f3a33a7c132d459bdbd4cc76eb)]

### Changed

- 💄 Adds a title to the Settings dialog [[77509f8](https://github.com/skyunlimitedinc/sky-launcher/commit/77509f8c04f26d620b7f169ef865509a581f3f47)]
- 💄 Updates icons [[61bf1b9](https://github.com/skyunlimitedinc/sky-launcher/commit/61bf1b95deef9681dca38fce4b29a52dcfe8b92c)]
- 🚚 Organizes dependencies [[a9b1bf0](https://github.com/skyunlimitedinc/sky-launcher/commit/a9b1bf0b2f60de5f6e7af1e7f8719f433919ca25)]
- 🎨 Tiny format fix in `pom.xml` [[70bf625](https://github.com/skyunlimitedinc/sky-launcher/commit/70bf625d2a9d8d2988e95a40dd756319524ce6bf)]
- 🔧 JetBrains config files updated [[15687c7](https://github.com/skyunlimitedinc/sky-launcher/commit/15687c74eed37159c2093977e29d1f3b69005335)]
- 📌 Fixes version of JetBrains Annotation dependency in Maven [[e227e1d](https://github.com/skyunlimitedinc/sky-launcher/commit/e227e1df8cee8b42c79c909e7a56b0d0c554c83b)]
- 🚸 Resets the logging and script directory prefs upon launch [[332f239](https://github.com/skyunlimitedinc/sky-launcher/commit/332f239160a1ffa10e63a173c6076723061d3992)]
- 💄 Adds the app icons to the "Finished!" dialog [[73a927c](https://github.com/skyunlimitedinc/sky-launcher/commit/73a927cb9d61a0f6376d4dfcde8dd32811e68b66)]
- 🚚 Refactors ArtDept to become the standard main one now [[85688ae](https://github.com/skyunlimitedinc/sky-launcher/commit/85688ae7aa5a6caa7762cafd4e620c32eea9d2da)]
- 🚚 Adjusts ScriptManager to use new ArtDept class [[4462658](https://github.com/skyunlimitedinc/sky-launcher/commit/4462658ba5a03feca7268985c62662205a2c21c7)]
- 🎨 Optimizes imports [[27e2b87](https://github.com/skyunlimitedinc/sky-launcher/commit/27e2b873944b54843b89b7fa5dde03a6bcc612ae)]
- ♻️ Updates Order Number validation [[8420bcf](https://github.com/skyunlimitedinc/sky-launcher/commit/8420bcf8dac45b408e61fc06db7688f938d8a152)]
- 💄 Refines size, location, and layout of windows [[c308268](https://github.com/skyunlimitedinc/sky-launcher/commit/c30826898ba1b69f86d87c8f7905e16491b428e4)]
- 💄 Sets app icon for both windows [[47b6250](https://github.com/skyunlimitedinc/sky-launcher/commit/47b6250b88185e6ad0ee283e54b3c7000b901cce)]
- ♻️ Refactors how the FlatLaf library is invoked once more [[dbc50f3](https://github.com/skyunlimitedinc/sky-launcher/commit/dbc50f396c72becb3f492ed1726a08716b232503)]
- ♻️ Refactors how the FlatLaf library is invoked [[ba5fb7a](https://github.com/skyunlimitedinc/sky-launcher/commit/ba5fb7a4d24902c3a830a5c32a69ce4c314c71b6)]
- ♻️ Refactors field validation in Settings [[0c27ca7](https://github.com/skyunlimitedinc/sky-launcher/commit/0c27ca761f8e1a638ef6a99695a69747ecbb01c7)]
- 💄 Adds Light and Dark Modes [[738885e](https://github.com/skyunlimitedinc/sky-launcher/commit/738885ebd850bbe1249ab6230e6b5ffebed87de6)]
- 🚸 Adds new, experimental main ArtDept form [[3833a4e](https://github.com/skyunlimitedinc/sky-launcher/commit/3833a4ed6401aad9931dda8152b97b56b8b64c9f)]
- 🏗️ Path TextField validator working [[50c9436](https://github.com/skyunlimitedinc/sky-launcher/commit/50c94364ee62ec61e4d2fbc5d4280f23d64c3e8a)]
- 🚚 Moves log4j2 config file [[fc75244](https://github.com/skyunlimitedinc/sky-launcher/commit/fc752448a13fc40caf83798ca399ff9f97963141)]
- 🚨 Removes linter warning about RegExp escapes [[cdef3f4](https://github.com/skyunlimitedinc/sky-launcher/commit/cdef3f415e479dd1175a354aa0af727cf90c5649)]
- 🚨 Some cleanup [[7803e91](https://github.com/skyunlimitedinc/sky-launcher/commit/7803e915b10532438226a7f33051de19f37b1d93)]
- ♻️ Some cleanup and renaming [[891b5c2](https://github.com/skyunlimitedinc/sky-launcher/commit/891b5c2df6289ab489707c200c9cd1a518f63fa7)]
- 🎨 Cleans up formatting in the Sanitizer [[c57578a](https://github.com/skyunlimitedinc/sky-launcher/commit/c57578aaa95ed44aa865c2e1d8e9ac40e5bb9c08)]

### Removed

- 🔥 Removes old Eclipse files [[2952434](https://github.com/skyunlimitedinc/sky-launcher/commit/2952434275ebd2ee9cdb03612fed7de203fda092)]
- 🔥 Removes unnecessary variable [[381b7ed](https://github.com/skyunlimitedinc/sky-launcher/commit/381b7ed74568e39dc6b1721f9a49e7f79fc1157f)]

### Fixed

- 🐛 Fixes the hanging thread issue [[7970aee](https://github.com/skyunlimitedinc/sky-launcher/commit/7970aee1bea138a1d98559d29fc499c3967efe98)]
- 🐛 Sets the correct radio button in Settings upon dialog opening [[cae3728](https://github.com/skyunlimitedinc/sky-launcher/commit/cae3728dbba34be21582d301599d39490f6c0c41)]
- 🐛 Fixes some pathname bugs [[cd8739b](https://github.com/skyunlimitedinc/sky-launcher/commit/cd8739b860512fca3ffae8f8fc0da5f7c73be110)]
- 🐛 Clean-up and bugfix [[f31c212](https://github.com/skyunlimitedinc/sky-launcher/commit/f31c212be64b17b7210876c75c9b117562da27c7)]
- 🚑 Fix app icons [[5cfc766](https://github.com/skyunlimitedinc/sky-launcher/commit/5cfc76636235cdeb1c705e5cbb7d4775e9582f69)]
- 🐛 Fixes references to log4j [[ebb49a2](https://github.com/skyunlimitedinc/sky-launcher/commit/ebb49a2009347adeae2dc55297e510eaf81ae48f)]

### Miscellaneous

- 🙈 Cleans up ignored files [[b9acb67](https://github.com/skyunlimitedinc/sky-launcher/commit/b9acb6769ae953af6a485ec3f797192e43becfe1)]
- 🙈 Ignores Maven files and executables [[df8c5ab](https://github.com/skyunlimitedinc/sky-launcher/commit/df8c5ab75cd23de93c77315d7ca55f48659e6694)]
- 📦 Stops the packager from being verbose [[848672e](https://github.com/skyunlimitedinc/sky-launcher/commit/848672e6572f6e1795c14678b9bc06eb3ebbd5cf)]
- 📦 Quote mark replacement and re-package [[7e25536](https://github.com/skyunlimitedinc/sky-launcher/commit/7e255361c8217e26320855801185bf5585cfa95b)]
- 📦 Uses `jpackage` to generate Windows executable and installer [[ef41dc6](https://github.com/skyunlimitedinc/sky-launcher/commit/ef41dc64292b37905ff735fc682de360a19a1a5f)]
- 📦 Creates executable JAR with Maven [[311ddd4](https://github.com/skyunlimitedinc/sky-launcher/commit/311ddd4f2d6b56e55a928863f6e770a1ce663934)]
- 📦 Further sets up executable JAR creation in IDEA [[671cc07](https://github.com/skyunlimitedinc/sky-launcher/commit/671cc07d4fd004e0d055084197f26a0c867ce344)]
- 📦 Setting up executable JAR creation in IDEA [[9943216](https://github.com/skyunlimitedinc/sky-launcher/commit/994321639a5014bacc796001402fa7a261ed2854)]
- 📸 Minor updates to `pom.xml` [[3e71127](https://github.com/skyunlimitedinc/sky-launcher/commit/3e711275380d84bb71e38df98c0f2fc8c45ff58c)]
- 🌐 I18nizes some more text in the Settings dialog [[e1af3ca](https://github.com/skyunlimitedinc/sky-launcher/commit/e1af3ca9d8378235aafdd4163ae431e9b363b612)]
- 🚧 Adds more settings [[6ffd56f](https://github.com/skyunlimitedinc/sky-launcher/commit/6ffd56fcbc0dbd9056808a4ffed36137ec15cff2)]
- 🤡 Whatever. [[69879f9](https://github.com/skyunlimitedinc/sky-launcher/commit/69879f9a94a2673b81080f8a8eefb4da34e0928b)]
- 🚧 Allow two classes to reference the same Preferences object [[a4fcdc2](https://github.com/skyunlimitedinc/sky-launcher/commit/a4fcdc242552b671a324b0e270dc63820ae61bb0)]
- 🌐 Adds compiled version of I18nizing properties file [[48e5eb3](https://github.com/skyunlimitedinc/sky-launcher/commit/48e5eb301d3b7879352312f3d097cbbb1dfcaef8)]
- 🌐 I18nizes hard-coded text in Settings form [[9017e7c](https://github.com/skyunlimitedinc/sky-launcher/commit/9017e7cc0af0e45d4983db3010a58f508e649da0)]
- 🚧 Assigning the active Script choice directory to a universal pref [[e5c61d1](https://github.com/skyunlimitedinc/sky-launcher/commit/e5c61d12b53ef0947ce6ff08f34140567b37dec1)]
- 🚧 Trying to get JTextField validators to work [[ca37b2c](https://github.com/skyunlimitedinc/sky-launcher/commit/ca37b2c52692329127807e9f8c59e5ac33a8d77b)]
- 🚧 Radio buttons mostly implemented [[7c56ef8](https://github.com/skyunlimitedinc/sky-launcher/commit/7c56ef8e679abcba90251c50627e681b69685d36)]
- 🚧 Implements all file choosers [[2f6e421](https://github.com/skyunlimitedinc/sky-launcher/commit/2f6e421a41c2b03ca6306d7e4fa649ed7f764d4f)]
- 🚧 Initial work on file chooser [[be55bc4](https://github.com/skyunlimitedinc/sky-launcher/commit/be55bc41bc26b6973dfe0cd639d821093c0075b7)]
- 🔨 Fixes Maven installation (hopefully) [[89a7b7a](https://github.com/skyunlimitedinc/sky-launcher/commit/89a7b7aefb751afdb3d1bfb8ff7b91199e3f7cdc)]
- 🚧 Begin work on the Script Location part of Settings [[694ec8f](https://github.com/skyunlimitedinc/sky-launcher/commit/694ec8f63aa9c74be7360ded651493f13e769a98)]
- 🚧 Adds Notification Email field & logic [[0c384d7](https://github.com/skyunlimitedinc/sky-launcher/commit/0c384d77b7759abf884b0add013280891e9fe8fc)]
- 🚧 Adds Initials field & logic to Settings [[e05eca2](https://github.com/skyunlimitedinc/sky-launcher/commit/e05eca2adc888d7a1c1671dd54631030e3d64c7b)]
- 🚧 Adds email text field and updates sanitizer [[9bcb0e3](https://github.com/skyunlimitedinc/sky-launcher/commit/9bcb0e3d005c418920af0bc8ecee3ea5ab320e7b)]
- 🚧 Moving Preferences to Settings class [[f618331](https://github.com/skyunlimitedinc/sky-launcher/commit/f61833107d5537c6df3ab2e158e9060c9c3ee17e)]


<a name="4.2.0"></a>
## 4.2.0 (2021-04-05)

### Changed

- 🚨 Removes IDE warnings from `FsTest.java` [[c3a897d](https://github.com/SturmB/sky-artdept-wrapper/commit/c3a897d47a2b8f6d57990a7d2595c7e16f0e7240)]
- 🎨 Formats code to be prettier [[12ef9f7](https://github.com/SturmB/sky-artdept-wrapper/commit/12ef9f77adacad29a1b2a0d2074e92cdb3bd3d51)]
- 🚨 Fixes IDE warnings for `ScriptManager.java` [[90d9a7c](https://github.com/SturmB/sky-artdept-wrapper/commit/90d9a7c6598bbb6f530dd8cb7af756261df8d57f)]
- 🎨 Reformats `ArtDept.java` [[5a4e847](https://github.com/SturmB/sky-artdept-wrapper/commit/5a4e847c12712580b62ce172f9a9aec28d2cea5d)]
- 🚨 Fixes rest of compiler warnings in `ArtDept.java` [[b7f9d1c](https://github.com/SturmB/sky-artdept-wrapper/commit/b7f9d1cce65f4b9d210f8ae3c4de9f8ef8bae54c)]
- 🚨 Fix most compiler warnings in `ArtDept.java` [[0284cf9](https://github.com/SturmB/sky-artdept-wrapper/commit/0284cf9088084231d018015ccec6ae6705383cc9)]
- ⬆️ Update JDK to Corretto 15 [[1240526](https://github.com/SturmB/sky-artdept-wrapper/commit/1240526cb4bcec7a78288f390e734956e9c6af0b)]
- 🚚 Cleaning things up after moving to IntelliJ IDEA [[cf825b4](https://github.com/SturmB/sky-artdept-wrapper/commit/cf825b407cfd79c18bf158513a05b38dfb3fe06c)]

### Miscellaneous

- 📦 Removes blocking signature files from dependencies [[68406ed](https://github.com/SturmB/sky-artdept-wrapper/commit/68406eddbe13c830b99b15caed70e09fc4887801)]
- 📦 Redirects and renames the completed jar file [[3e72732](https://github.com/SturmB/sky-artdept-wrapper/commit/3e72732a9f3bd2bed817b1b98de56a748ab30864)]
- 🚧 Adds library files to repo [[771f203](https://github.com/SturmB/sky-artdept-wrapper/commit/771f2036c25fdb28404f6a557e6975ea0e89a6f3)]
- 🚧 Still working on fixing artifacts [[8b58eff](https://github.com/SturmB/sky-artdept-wrapper/commit/8b58eff12405f2ce9bd4d72796e77f4bcaa9380a)]
- 🚧 Working on fixing artifacts [[3690742](https://github.com/SturmB/sky-artdept-wrapper/commit/369074200065a6b25ba772725df4cd974a8b04b6)]

<a name="4.1.0"></a>
## 4.1.0 (2021-02-10)

### Added

- ✨ Clears Redis caches upon successful run of scripts. [[6603e5b](https://github.com/SturmB/sky-artdept-wrapper/commit/6603e5bd8a5fc30a6495b2a4827a46a25bb6e736)]
- ➕ Adds Lettuce for Redis integration [[e4fd9a9](https://github.com/SturmB/sky-artdept-wrapper/commit/e4fd9a93c3e8770db3076ff6b8ca40f7660b661b)]

### Changed

- 🚨 Removes warnings [[84b913e](https://github.com/SturmB/sky-artdept-wrapper/commit/84b913e8ebab1028bb64325a0a15e1001fbb4485)]

### Miscellaneous

- 🚧 Completes RedisManager [[f9e490e](https://github.com/SturmB/sky-artdept-wrapper/commit/f9e490e87f01ef702c8b9239acd00b624ffdac2e)]
- 🚧 Connects to Redis [[fa87565](https://github.com/SturmB/sky-artdept-wrapper/commit/fa87565cfc68d382c995acdc67081376e029f068)]

<a name="4.0.5"></a>
## 4.0.5 (2019-09-30)

### Miscellaneous

- Null Handler [[61253a6](https://github.com/SturmB/sky-artdept-wrapper/commit/61253a64e8d80fd408b05b9d696c0d14ba41e4cb)]

<a name="4.0.4"></a>
## 4.0.4 (2019-09-30)

### Miscellaneous

- Sync [[97c226a](https://github.com/SturmB/sky-artdept-wrapper/commit/97c226a1368a2223236c36ddfc24f4603b55b59d)]

<a name="4.0.2"></a>
## 4.0.2 (2019-05-31)

### Miscellaneous

- Callable Attempt [[111112d](https://github.com/SturmB/sky-artdept-wrapper/commit/111112dc791cb7e299b24136a0bd262ff7d4a86d)]
- Runtime.exec hangs, part 1 [[372925f](https://github.com/SturmB/sky-artdept-wrapper/commit/372925f91f8c15ba9ff28820f6b5912d77dbc3ac)]

<a name="4.0.1"></a>
## 4.0.1 (2019-05-29)

### Miscellaneous

- In Parity [[349dfbc](https://github.com/SturmB/sky-artdept-wrapper/commit/349dfbc74650df0fb5d2e93b79d0bc062778c09b)]
- Merge to 4.0.1 [[0833055](https://github.com/SturmB/sky-artdept-wrapper/commit/083305506ded97bd4230c338a5857a370b33d8bc)]

<a name="4.0.0"></a>
## 4.0.0 (2019-05-29)

### Miscellaneous

- Merge branch 'master' of ssh://git@github.com/SturmB/sky-artdept-wrapper.git [[9eaeaa3](https://github.com/SturmB/sky-artdept-wrapper/commit/9eaeaa3d255b2e81d23f2e85f7d92c7416dd76e2)]
- OVERRUNS Fix [[e4b0c9d](https://github.com/SturmB/sky-artdept-wrapper/commit/e4b0c9d803af13af669efb1ede0976f7cb6e703f)]
- Windows [[f4876ae](https://github.com/SturmB/sky-artdept-wrapper/commit/f4876ae59578ca00d4730a0ce13453d0dcdd5099)]

<a name="3.8.7"></a>
## 3.8.7 (2019-04-12)

### Miscellaneous

- Overruns Fix [[ce7c818](https://github.com/SturmB/sky-artdept-wrapper/commit/ce7c818400251f292f9c7401cd22265ef5264f95)]

<a name="3.8.6"></a>
## 3.8.6 (2018-07-19)

### Miscellaneous

- Minor edit. [[3976044](https://github.com/SturmB/sky-artdept-wrapper/commit/39760442b192204bb2a417d4e23ca5fd56be516c)]
- MySQL 8 and Impressions [[176f66d](https://github.com/SturmB/sky-artdept-wrapper/commit/176f66d0c3c19cb847b319b1be306063733e6cd9)]
- Screen Cups Bugfix [[8330b79](https://github.com/SturmB/sky-artdept-wrapper/commit/8330b792d526c3c126f9608f158278520c0de242)]
- Malleable Item Status [[e97cfdb](https://github.com/SturmB/sky-artdept-wrapper/commit/e97cfdb7c30d9d9b744df83207c0a180c94d1044)]
- Package & Case Quantities [[63de6b1](https://github.com/SturmB/sky-artdept-wrapper/commit/63de6b1302dcef1e38af2695e258b8ec07344bf9)]

<a name="3.8.0"></a>
## 3.8.0 (2018-06-21)

### Miscellaneous

- SkyUbuntu Ready [[06bf49a](https://github.com/SturmB/sky-artdept-wrapper/commit/06bf49a2af2b27d519194da3f47887f79a5148b0)]

<a name="3.80"></a>
## 3.80 (2018-02-15)

### Miscellaneous

- Major Web App Update [[63e4dc9](https://github.com/SturmB/sky-artdept-wrapper/commit/63e4dc92f1b2c5a474c5ed6c6ebe4e8dbe7f0421)]
- Merge branch 'master' of ssh://git@github.com/SturmB/sky-artdept-wrapper.git [[9ed7806](https://github.com/SturmB/sky-artdept-wrapper/commit/9ed78066203ec46696dc9da0e84fbaceff4f03c3)]

<a name="3.46"></a>
## 3.46 (2018-02-12)

### Miscellaneous

- Title [[075ea91](https://github.com/SturmB/sky-artdept-wrapper/commit/075ea91d35ca67f278c7c8b2392ee769609b544c)]
- Final Fix of Duplicate Artwork Problem [[abd2529](https://github.com/SturmB/sky-artdept-wrapper/commit/abd2529437ae691e153201a68f0cc276b55e55d2)]

<a name="3.45"></a>
## 3.45 (2018-01-18)

### Miscellaneous

- No More Invalid Entries in Artwork [[673cd52](https://github.com/SturmB/sky-artdept-wrapper/commit/673cd524b8b33472f1f3adad662313aa23882b96)]

<a name="3.41"></a>
## 3.41 (2018-01-17)

### Miscellaneous

- Various Old Tweaks and Updates [[e1660a5](https://github.com/SturmB/sky-artdept-wrapper/commit/e1660a5dea8f5069044d319962d8f5bbaee56230)]

<a name="3.31"></a>
## 3.31 (2017-04-12)

### Miscellaneous

- Production Relocation [[52975a7](https://github.com/SturmB/sky-artdept-wrapper/commit/52975a7468ddc0e99c7be467e5fb1775e3f8f0f4)]

<a name="3.30"></a>
## 3.30 (2017-04-05)

### Miscellaneous

- JSON Update [[9f8a2da](https://github.com/SturmB/sky-artdept-wrapper/commit/9f8a2daad7d57f36fdafcb8c4500e5d6822facd6)]

<a name="3.10"></a>
## 3.10 (2016-12-05)

### Miscellaneous

- 2017 Changes (try [#2](https://github.com/SturmB/sky-artdept-wrapper/issues/2)) [[c966691](https://github.com/SturmB/sky-artdept-wrapper/commit/c96669101ece0772176222e8fd2c8d29b4aa8601)]
- 2017 Changes [[2d0ede3](https://github.com/SturmB/sky-artdept-wrapper/commit/2d0ede3f3e009cc1ed389882e0567393938d183a)]

<a name="2.12"></a>
## 2.12 (2016-07-07)

### Miscellaneous

- Fixed bug in `insert` method of the `DayManager` class. One comma was missing in the SQL statement. [[d0d0865](https://github.com/SturmB/sky-artdept-wrapper/commit/d0d08656f5c5d84a59ab589bc587d4a0ebf52f50)]

<a name="2.11 (Test)"></a>
## 2.11 (Test) (2016-07-01)

### Miscellaneous

- Settable Defaults & A New Print Method [[282ca66](https://github.com/SturmB/sky-artdept-wrapper/commit/282ca66505a047365fd0f651aeda48f13313b0f8)]

<a name="2.00"></a>
## 2.00 (2016-06-15)

### Miscellaneous

- Fix. [[3f24405](https://github.com/SturmB/sky-artdept-wrapper/commit/3f2440524b7e39f2dfffb714a4d4115fc62a83e2)]
- Updated to v2.00 [[3ab9d8f](https://github.com/SturmB/sky-artdept-wrapper/commit/3ab9d8fcd9eb21bac03671577ce616e6aa0218b2)]

<a name="1.90"></a>
## 1.90 (2016-04-19)

### Miscellaneous

- Fix Lockup on Key Phrase Detection [[7558a52](https://github.com/SturmB/sky-artdept-wrapper/commit/7558a52d843cd7d7c9e451c19e1d51b5232f2de6)]

<a name="1.89"></a>
## 1.89 (2016-04-18)

### Miscellaneous

- Minor update to remove the /logs folder from git. [[d6cbe5c](https://github.com/SturmB/sky-artdept-wrapper/commit/d6cbe5cc1d0d8209bd9f1a37afc730996b234109)]
- More Data Mining [[65ff83c](https://github.com/SturmB/sky-artdept-wrapper/commit/65ff83c3eb49061215d7f876f719f75ee99e526d)]

<a name="1.85"></a>
## 1.85 (2016-04-05)

### Miscellaneous

- Added README.md file. [[5cd7c93](https://github.com/SturmB/sky-artdept-wrapper/commit/5cd7c936c29fc56d8d2365a90fd58eab25b7bafa)]
- Initial commit. [[c42af6a](https://github.com/SturmB/sky-artdept-wrapper/commit/c42af6a78fd2d564ce2f99d7e8cdb53e70a57408)]
