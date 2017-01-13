#!/usr/bin/env bash

#$1 should be the BranchID
#$2 should be the repository e.g. https://github.com/MyCoRe-Travis/test_artifacts.git

git clone $2 ./autodeploy
cd ./autodeploy
git branch $1
git checkout $1
echo "Travis: https://travis-ci.org/MyCoRe-Org/mycore/builds/$TRAVIS_BUILD_ID \nMycore-Pull: https://github.com/MyCoRe-Org/mycore/pull/$TRAVIS_PULL_REQUEST \nCommit: https://github.com/MyCoRe-Org/mycore/commit/$TRAVIS_COMMIT \n $TRAVIS_PULL_REQUEST_BRANCH \n $TRAVIS_PULL_REQUEST_SHA \n $TRAVIS_EVENT_TYPE " > README.md
cd ../

mkdir -p autodeploy/mycore-viewer/failsafe-reports/
cp -r mycore-viewer/target/failsafe-reports/ autodeploy/mycore-viewer/failsafe-reports/
cp -r mycore-viewer/target/result/screenshots/ autodeploy/mycore-viewer/screenshots

cd ./autodeploy
git add .
git commit -m "adding test results"
git push -f --set-upstream origin $1
