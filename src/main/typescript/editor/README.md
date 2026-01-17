## How to update:
- Enter the editor source directory using `cd src/main/typescript/editor`
- Remove any existing `package-lock.json`
- Run `npx npm-check-updates -u`, which finds new versions of each package in the `package.json` file
- Run `npm i` to install new dependencies
## How to build:
- Run `npm run build` to produce dist/index.html
- That's it, just copy `dist/index.html` to deploy/html/editor.html!
