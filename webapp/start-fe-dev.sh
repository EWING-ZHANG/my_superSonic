rm -rf ./packages/supersonic-fe/src/.umi ./packages/supersonic-fe/src/.umi-production

npm i

npx lerna add supersonic-chat-sdk --scope supersonic-fe

npx lerna bootstrap

npx lerna exec --scope supersonic-chat-sdk npm run build

npx lerna exec --scope supersonic-fe npm start
