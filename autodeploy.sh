#!/usr/bin/env bash

#$1 should be the BranchID
#$2 should be the repository e.g. https://github.com/paschty/Test_Deploy_Test.git

git clone $2 ./autodeploy
cd ./autodeploy
git branch $1
git checkout $1
cd ../

mkdir -p autodeploy/mycore-viewer/failsafe-reports/
cp -r mycore-viewer/target/failsafe-reports/ autodeploy/mycore-viewer/failsafe-reports/
cp -r mycore-viewer/target/result/screenshots/ autodeploy/mycore-viewer/screenshots

cd ./autodeploy
git add .
git commit -m "adding test results"
git push -f --set-upstream origin $1
