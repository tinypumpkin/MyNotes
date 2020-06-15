#!/bin/bash 
es_home=/opt/module/elasticsearch
kibana_home=/opt/module/kibana
case $1  in
 "start") {
  for i in hadoop100 hadoop101 hadoop102
  do
    ssh $i  "source /etc/profile;${es_home}/bin/elasticsearch >/dev/null 2>&1 &"
 
   done
 
};;
"stop") {
  for i in hadoop100 hadoop101 hadoop102
  do
      ssh $i "ps -ef|grep $es_home |grep -v grep|awk '{print \$2}'|xargs kill" >/dev/null 2>&1
  done
  
};;
esac
