该项目主要完成的功能为  
1.导入批量的数据  
2.航信模型处理并将结果写入文件中
项目由于需要部署在航信的服务器上，部分文件才有加密的形式
启动方式 nohup java -javaagent:hxmodels-1.0.0-SNAPSHOT-encrypted.jar -jar hxmodels-1.0.0-SNAPSHOT-encrypted.jar &
其中 hxmodels-1.0.0-SNAPSHOT-encrypted.jar 为加密后的jar  
目前还有xml 没有实现加密操作