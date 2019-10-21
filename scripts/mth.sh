#!/bin/bash

# set -e

# Move an existing git repository to my "hacks" repository. (move to hacks)
HACKS_PATH="$HOME/projects/hacks"
README_PATH="$HACKS_PATH/README.md"

if [ -z "$1" ]; then
    echo "Usage: mth [REPO_PATH]"
    exit 1
else
    # handle absolute and relative paths
    tmp=$1
    if [[ $tmp == /* ]]; then
        REPO_PATH=$tmp
    else 
        REPO_PATH="$(pwd)/$tmp"
    fi    
fi
REPO_NAME=$(basename -- $REPO_PATH)
DEST_PATH="$HACKS_PATH/$REPO_NAME"

echo
echo " HACKS_PATH: $HACKS_PATH"
echo "README_PATH: $README_PATH"
echo "  REPO_PATH: $REPO_PATH"
echo "  REPO_NAME: $REPO_NAME"
echo "  DEST_PATH: $DEST_PATH"
echo

# handle case where $HACKS_PATH/foo already exists
if [ -d "$DEST_PATH" ]; then
    echo "$DEST_PATH already exists!"
    exit
fi

mkdir -p "${DEST_PATH}"

cd $REPO_PATH

declare -A files
files=$(git ls-files)

for f in "$files"
do 
  printf "copying %s\n" $f
  cp --parents $f "${DEST_PATH}/"
done

printf "* [%s](%s)\n" ${REPO_NAME} ${REPO_NAME} | tee -a $README_PATH

cd $HACKS_PATH

git status

echo "Adding $REPO_NAME"
git add $REPO_NAME
echo "Added $REPO_NAME"

echo "Committing $REP_NAME"
git commit -am "Added $REPO_NAME"
echo "Committed"

echo "Pushing"
git push
echo "Pushed"

git status

cd -

echo "Done!"
