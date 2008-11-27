#!/bin/sh

# Source folder
SRC="/Development/AMEE.git"

# Destination folder
DST="/Development/AMEE.deploy"

# bin
mkdir -p $DST/bin
cp -r $SRC/server/co2-engine/bin/* $DST/bin

# conf
mkdir -p $DST/conf
cp -r $SRC/server/co2-engine/conf/* $DST/conf

# htdocs
mkdir -p $DST/htdocs
cp -r $SRC/htdocs/* $DST/htdocs

# skins
mkdir -p $DST/skins
cp -r $SRC/server/skins/* $DST/skins

# lib
mkdir -p $DST/lib
cp -r $SRC/lib/* $DST/lib

# logs
mkdir -p $DST/logs