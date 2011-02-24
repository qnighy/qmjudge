#!/bin/sh
cat qmjudge.fc >> /etc/selinux/default/contexts/files/file_contexts.local
restorecon -RF /home

