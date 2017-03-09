#!/bin/bash
user_input=$1
if [[ -z "${user_input// }" ]]
then
  echo 'Please input one file or dir name.'
  exit
else
 echo "Your input is: $user_input"
fi

if [[ -d $user_input ]];
then
 icounts=$( ls -l $user_input | wc -l )
 echo Direcotry $user_input has : $icounts items in.
#echo Direcotry $user_input has : `ls -l $user_input | wc -l` items in.
elif [[ -f $user_input ]];
then
 echo File $user_input is `ls -l ${user_input} | awk '{print $5}'` bytes
else
 echo "$user_input is not valid type"
fi
