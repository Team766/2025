## How this folder can be reproduced
- Download https://registry.npmjs.org/monaco-editor/-/monaco-editor-0.55.1.tgz, or whatever version of monaco you want
- Decompress tgz file, a folder called package should be created
- Copy the folder `package/min/vs` into the static files folder, next to the editor.html file
- This vs folder should be functional, however it is also quite bloated. To make it smaller, head over to the network tab in the browser developer tools to find which files are really necessary for it to function, and delete the ones that aren't. This helps make it more manageable both for github and for the RoboRIO
    - **If you've decided to trim the folder, be sure to test all features afterwards. It is very feasible to take 10+ megabytes off of it, but deleting the wrong thing can break things badly.**