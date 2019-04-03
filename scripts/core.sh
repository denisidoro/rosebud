#!/usr/bin/env bash
# vim: filetype=sh

if [ -z ${TERM:-} ] || [ $TERM = "dumb" ]; then
  bold=""
  underline=""
  freset=""
  purple=""
  red=""
  green=""
  tan=""
  blue=""
else
  bold=$(tput bold)
  underline=$(tput sgr 0 1)
  freset=$(tput sgr0)
  purple=$(tput setaf 171)
  red=$(tput setaf 1)
  green=$(tput setaf 76)
  tan=$(tput setaf 3)
  blue=$(tput setaf 38)
fi

function _log() {
  local template=$1
  shift
  echo -e $(printf "$template" "$@")
}

function success() { _log "${green}✔ %s${freset}\n" "$@"; }
function error() { _log "${red}✖ %s${freset}\n" "$@"; }
function warning() { _log "${tan}➜ %s${freset}\n" "$@"; }
function note() { _log "${underline}${bold}${blue}Note:${freset} ${blue}%s${freset}\n" "$@"; }
