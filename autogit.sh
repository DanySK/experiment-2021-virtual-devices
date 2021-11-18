#!/bin/bash
for file in data/*
  do LASTLINE="$(tail -n 1 "$file")"
  if [ "${LASTLINE:0:1}" == "#" ]
    then git add -f "${file}"
  fi
done
COMMIT_MESSAGE="[AUTOMATIC ${USER}@${HOSTNAME}] - `date --iso-8601=minutes` data update"
git commit -m "${COMMIT_MESSAGE}"
git pull --rebase=false --no-edit
git push
